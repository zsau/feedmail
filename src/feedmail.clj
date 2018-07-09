(ns feedmail
	(:gen-class)
	(:import [java.io PushbackReader ByteArrayInputStream])
	(:require
		[clj-http.client :as http]
		[clj-time.core :as time]
		[clj-time.format :as time.fmt]
		[clojure.core.memoize :as memo]
		[clojure.java.shell :as shell]
		[clojure.string :as str]
		[clojure.tools.cli :as cli]
		[feedparser-clj.core :as feed]
		[mail]
		[net.cgrand.enlive-html :as html]))

; based on https://gist.github.com/Gonzih/5814945
(defmacro try+ "Like try, but can catch multiple exception types with (catch+ [classname*] name expr)."
	[& body]
	(letfn [
			(catch+? [form]
				(and (seq form)
					(= (first form) 'catch+)))
			(expand [[_catch* classes & catch-tail]]
				(map #(list* 'catch % catch-tail) classes))
			(transform [form]
				(if (catch+? form)
					(expand form)
					[form]))]
		(cons 'try (mapcat transform body))))

(defn sha1-bytes [s]
	(.digest (java.security.MessageDigest/getInstance "SHA1")
		(.getBytes s java.nio.charset.StandardCharsets/UTF_8)))

(defn sha1 [s]
	(.toLowerCase
		(javax.xml.bind.DatatypeConverter/printHexBinary
			(sha1-bytes s))))

(defn die [status message]
	(binding [*out* *err*]
		(println message))
	(System/exit status))

(defn read-config [path]
	(with-open [in (PushbackReader. (clojure.java.io/reader path))]
		(eval (read in)))) ; !!! DANGER: evals any code in the config !!!

(defn cache-path [config url]
	(str (:path (:cache config)) "/" (sha1 url)))

(defn recipient [imap]
	(str (:user imap) "@" (:host imap)))

(defn read-cache [path]
	(try
		(let [[date & ids] (str/split-lines (slurp path))]
			{:date date :ids ids})
		(catch java.io.FileNotFoundException e)))

(defn write-cache [path cache]
	(spit path
		(str/join "\n"
			(cons (:date cache)
				(:ids cache)))))

(defn item-date [item]
	((some-fn :updated-date :published-date) item))

(defn item-author [feed item]
	(let [author (first (:authors item))] {
		:email (or (:email author) "feedmail@localhost")
		:name (or
			(:name author)
			(:author item) ; allows (assoc % :author "Bob") in config files
			(:title feed))}))

(defn item-content [item]
	((some-fn :content :description) item))

(defn email-template [s]
	(html/template (ByteArrayInputStream. (.getBytes s "UTF-8")) [item]
		[:a.link] (html/set-attr :href (:uri item))
		[:.title] (html/content (:title item))
		[:.content]
			(let [{:keys [value type]} (item-content item)]
				(if (#{"html" "text/html" "xhtml"} type)
					(html/html-content value)
					(html/content value)))))

(defn sort-items [items]
	(if (item-date (first items))
		(sort-by item-date #(compare %2 %1) items)
			items))

(defn resolve-uri [uri base]
	(if uri
		(.toString (.resolve (java.net.URI. base) uri))
		base))

(defn resolve-links
	([base] ; transducer
		(map #(update-in % [:uri] resolve-uri base)))
	([base items]
		(into [] (resolve-links base) items)))

(defn item->email [config feed item]
	(let [author (item-author feed item)] {
		:from (mail/address (:email author) (:name author))
		:to (mail/address (recipient (:imap config)))
		:subject (:title item)
		:date (item-date item)
		:body (clojure.string/join
			((email-template (:template (:email config)))
				item))}))

(defn parse-feed [body]
	(when body
		(feed/parse-feed
			(ByteArrayInputStream.
				(.getBytes body "UTF-8")))))

(defn script-get [path]
	(let [{:keys [exit out]} (shell/sh path)] ; !!! DANGER: running arbitrary external command from config !!!
		{:body out, :status (if (zero? exit) 200 500)}))

(defn fetch* [uri date]
	(let [uri (java.net.URI. uri)]
		(if (= "script" (.getScheme uri))
			(script-get (.getSchemeSpecificPart uri))
			(http/get (.toString uri) {
				:http-builder-fns [(fn [builder _] (.disableCookieManagement builder))]
				:headers (if date {"If-Modified-Since" date} {})
				:throw-exceptions false
				:decode-cookies false}))))

; Memoize fetched URLs to facilitate using the same URL in more than one subscription (e.g. a combined feed that's split via multiple filters). A FIFO cache of size 1 suffices, since we sort subscriptions by URL before fetching.
(def memo-fetch (memo/fifo fetch* :fifo/threshold 1))

(defn fetch [url date]
	(let [{:keys [status body]} (memo-fetch url date)]
		(condp = status
			200 body
			304 nil
			(throw (java.io.IOException. (str "couldn't fetch feed: HTTP " status))))))

(defn report-feed-error [url e]
	(binding [*out* *err*]
		(println (format "Error on feed: %s\n%s"
			url (.toString e)))))

(defn check-subscription [config store date {:keys [url] :as subscription}]
	(when (:verbose config)
		(println "checking" url))
	(try+
		(let [
				map-fn (or (:map subscription) identity)
				filter-fn (or (:filter subscription) identity)
				cache-path (cache-path config url)
				cache (read-cache cache-path)
				body (fetch url (:date cache))
				feed (parse-feed body)
				new-items (into []
					(comp
						(take (:size (:cache config)))
						(keep map-fn)
						(filter filter-fn)
						(filter (comp (complement (set (:ids cache))) :uri))
						(resolve-links url))
					(sort-items (:entries feed)))]
			(when (:verbose config)
				(println "-> got" (count new-items) "new items")
				(doseq [e new-items]
					(println e)))
			(when-not (:dry-run config)
				(mail/append-messages store {:name (:folder subscription) :create true}
					(map (partial item->email config feed)
						(reverse new-items)))
				(write-cache cache-path {
					:date date
					:ids
						(take (:size (:cache config))
							(concat (map :uri new-items)
								(:ids cache)))})))
		; ROME throws IllegalArgumentException sometimes for invalid documents
		(catch+ [IllegalArgumentException java.io.IOException java.net.ConnectException java.net.UnknownHostException javax.mail.MessagingException com.rometools.rome.io.FeedException org.apache.http.HttpException] e
			(when-not (:suppress-errors subscription) (report-feed-error url e)))
		(catch Exception e (report-feed-error url e) (throw e))))

(defn check-subscriptions [{:keys [imap subscriptions] :as config} subscription-names-to-check]
	(let [
			filter-fn (if (seq subscription-names-to-check) (comp (set subscription-names-to-check) :name) any?)
			date (time.fmt/unparse (time.fmt/formatters :rfc822) (time/now))]
		(mail/with-store [store imap]
			; sort by url so our cache can work more effectively
			(doseq [subscription (sort-by :url (filter filter-fn subscriptions))]
				(check-subscription config store date subscription)))))

(defn usage [options-summary]
	(format "usage: feedmail [options] [FEED_NAME ...]\n\nOptions:\n%s"
		options-summary))

(def cli-options [
	["-h" "--help"]
	["-v" "--verbose"]
	["-d" "--dry-run" "Don't upload emails or update cache"]
	["-c" "--config FILE" "Config file path"
		:default (str (System/getProperty "user.home") "/.config/feedmail/config.clj")
		:validate [#(.exists (java.io.File. %)) "no such file"]]]) ;FIXME: real validation (spec?)

(def default-config {
	:cache {
		:path (str (System/getProperty "user.home") "/.cache/feedmail")
		:size 100}
	:email {
		:template "<h1><a class='link title'></a></h1><p class='content'></p>"}
	:imap {
		:host nil
		:user nil
		:password nil}
	:subscriptions
		(take 0 [{ ; just for documentation:
			:url "http://example.com/"
			:folder "Folder/Subfolder"
			:filter any?
			:map identity
			:suppress-errors false}])})

(defn -main [& args]
	(let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
		(cond
			(:help options)
				(println (usage summary))
			errors
				(die 1 (str/join "\n" errors))
			:else
				(let [config (merge default-config
						(read-config (:config options))
						(select-keys options [:verbose :dry-run]))]
					(when (:verbose config)
						(println "config:" (update-in config [:imap] dissoc :password)))
					(.mkdir (java.io.File. (:path (:cache config))))
					(check-subscriptions config arguments)
					(shutdown-agents)))))

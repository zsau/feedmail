(defproject zsau/feedmail "0.2.8"
	:description "Uploads RSS/Atom feed items as emails via IMAP."
	:url "http://example.com/FIXME"
	:license {
		:name "Eclipse Public License"
		:url "http://www.eclipse.org/legal/epl-v10.html"}
	:aot [feedmail]
	:main feedmail
	:dependencies [
		[ch.qos.logback/logback-classic "1.2.3"]
		[clj-http "3.10.0"]
		[clojure.java-time "0.3.2"]
		[com.taoensso/timbre "4.10.0"]
		[enlive "1.1.6"]
		[org.clojure/clojure "1.10.1"]
		[org.clojure/core.memoize "0.8.2"]
		[org.clojure/tools.cli "0.4.2"]
		[zsau/feedparser-clj "0.6.3"]
		[zsau/mail "0.1.0"]])

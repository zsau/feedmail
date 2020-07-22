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
		[clj-http "3.10.1"]
		[clojure.java-time "0.3.2"]
		[com.taoensso/timbre "4.10.0"]
		[enlive "1.1.6"]
		[org.clojure/clojure "1.10.1"]
		[org.clojure/core.memoize "1.0.236"]
		[org.clojure/tools.cli "1.0.194"]
		[zsau/rome-clj "1.0.0"]
		[zsau/mail "0.1.1"]])

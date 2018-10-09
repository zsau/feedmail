(defproject zsau/feedmail "0.2.6"
	:description "Uploads RSS/Atom feed items as emails via IMAP."
	:url "http://example.com/FIXME"
	:license {
		:name "Eclipse Public License"
		:url "http://www.eclipse.org/legal/epl-v10.html"}
	:aot [feedmail]
	:main feedmail
	:dependencies [
		[org.clojure/clojure "1.9.0"]
		[org.clojure/core.memoize "0.7.1"]
		[org.clojure/tools.cli "0.4.1"]
		[clj-time "0.14.4"]
		[clj-http "3.9.1"]
		[enlive "1.1.6"]
		[zsau/feedparser-clj "0.6.2"]
		[ch.qos.logback/logback-classic "1.2.3"]
		[zsau/mail "0.1.0"]])

(defproject zsau/feedmail "0.1.0"
	:description "RSS/Atom feed client that converts items to emails"
	:url "https://github.com/zsau/feedmail"
	:license {
		:name "MIT License"
		:url "https://opensource.org/licenses/MIT"}
	:aot [feedmail]
	:main feedmail
	:dependencies [
		[ch.qos.logback/logback-classic "1.2.3"]
		[clj-http "3.8.0"]
		[clj-time "0.14.2"]
		[enlive "1.1.6"]
		[org.clojure/clojure "1.9.0"]
		[org.clojure/core.memoize "0.7.1"]
		[org.clojure/tools.cli "0.3.5"]
		[zsau/feedparser-clj "0.6.0"]
		[zsau/mail "0.1.0"]])

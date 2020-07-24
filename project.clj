(defproject zsau/feedmail "0.2.8"
	:description "RSS/Atom feed client that converts items to emails"
	:url "https://github.com/zsau/feedmail"
	:license {
		:name "MIT License"
		:url "https://opensource.org/licenses/MIT"}
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
		[zsau/rome-clj "1.0.2"]
		[zsau/mail "0.1.1"]])

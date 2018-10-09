(ns feedmail-test
	(:require
		[clojure.test :refer :all]
		[feedmail :refer :all]))

; RSS samples from https://cyber.harvard.edu/rss/rss.html
; Atom sample from https://validator.w3.org/feed/docs/atom.html
(def feeds {
	"atom-1.0"
		{:authors [{:email nil, :name "John Doe", :uri nil}], :categories [], :contributors [], :copyright nil, :description nil, :encoding nil, :feed-type "atom_1.0", :image nil, :language nil, :link "http://example.org/", :entry-links [{:href "http://example.org/", :hreflang nil, :length 0, :rel "alternate", :title nil, :type nil}], :published-date #inst "2003-12-13T18:30:02.000-00:00", :title "Example Feed", :uri "urn:uuid:60a76c80-d399-11d9-b93C-0003939e0af6", :entries [
			{:authors [], :categories [], :content nil, :contributors [], :description {:type nil, :value "Some text."}, :enclosures [], :link "http://example.org/2003/12/13/atom03", :published-date nil, :title "Atom-Powered Robots Run Amok", :updated-date #inst "2003-12-13T18:30:02.000-00:00", :url nil, :uri "urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a"}]}
	"rss-2.0"
		{:authors [], :categories [], :contributors [], :copyright nil, :description "Liftoff to Space Exploration.", :encoding nil, :feed-type "rss_2.0", :image nil, :language "en-us", :link "http://liftoff.msfc.nasa.gov/", :entry-links [], :published-date #inst "2003-06-10T04:00:00.000-00:00", :title "Liftoff News", :uri nil, :entries [
			{:authors [], :categories [], :content nil, :contributors [], :description {:type "text/html", :value "How do Americans get ready to work with Russians..."}, :enclosures [], :link "http://liftoff.msfc.nasa.gov/news/2003/news-starcity.asp", :published-date #inst "2003-06-03T09:39:21.000-00:00", :title "Star City", :updated-date nil, :url nil, :uri "http://liftoff.msfc.nasa.gov/2003/06/03.html#item573"}
			{:authors [], :categories [], :content nil, :contributors [], :description {:type "text/html", :value "Sky watchers in Europe, Asia, and parts of Alaska and Canada..."}, :enclosures [], :link "http://liftoff.msfc.nasa.gov/2003/05/30.html#item572", :published-date #inst "2003-05-30T11:06:42.000-00:00", :title nil, :updated-date nil, :url nil, :uri "http://liftoff.msfc.nasa.gov/2003/05/30.html#item572"}
			{:authors [], :categories [], :content nil, :contributors [], :description {:type "text/html", :value "Before man travels to Mars, NASA hopes to design new engines..."}, :enclosures [], :link "http://liftoff.msfc.nasa.gov/news/2003/news-VASIMR.asp", :published-date #inst "2003-05-27T08:37:32.000-00:00", :title "The Engine That Does More", :updated-date nil, :url nil, :uri "http://liftoff.msfc.nasa.gov/2003/05/27.html#item571"}]}
	"rss-0.9.2"
		{:authors [], :categories [], :contributors [], :copyright nil, :description "A high-fidelity Grateful Dead song every day. This is where we're experimenting with enclosures on RSS news items that download when you're not using your computer. If it works (it will) it will be the end of the Click-And-Wait multimedia experience on the Internet. ", :encoding nil, :feed-type "rss_0.92", :image nil, :language nil, :link "http://www.scripting.com/blog/categories/gratefulDead.html", :entry-links [], :published-date #inst "2001-04-13T19:23:02.000-00:00", :title "Dave Winer: Grateful Dead", :uri nil, :entries [
			{:authors [], :categories [], :content nil, :contributors [], :description {:type "text/html", :value "It's been a few days since I added a song..."}, :enclosures [{:length 6182912, :type "audio/mpeg", :uri nil, :url "http://www.scripting.com/mp3s/weatherReportDicksPicsVol7.mp3"}], :link nil, :published-date nil, :title nil, :updated-date nil, :url nil, :uri nil}
			{:authors [], :categories [], :content nil, :contributors [], :description {:type "text/html", :value "Kevin Drennan started a <a href=\"http://deadend.editthispage.com/\">Grateful Dead Weblog</a>..."}, :enclosures [], :link nil, :published-date nil, :title nil, :updated-date nil, :url nil, :uri nil}
			{:authors [], :categories [], :content nil, :contributors [], :description {:type "text/html", :value "<a href=\"http://arts.ucsc.edu/GDead/AGDL/other1.html\">The Other One</a>, live instrumental, One From The Vault..."}, :enclosures [{:length 6666097, :type "audio/mpeg", :uri nil, :url "http://www.scripting.com/mp3s/theOtherOne.mp3"}], :link nil, :published-date nil, :title nil, :updated-date nil, :url nil, :uri nil}]}
	"rss-0.9.1"
		{:authors [], :categories [], :contributors [], :copyright "Copyright 2000, WriteTheWeb team.", :description "News for web users that write back", :encoding nil, :feed-type "rss_0.91U", :image {:description "News for web users that write back", :link "http://writetheweb.com", :title "WriteTheWeb", :url "http://writetheweb.com/images/mynetscape88.gif"}, :language "en-us", :link "http://writetheweb.com", :entry-links [], :published-date nil, :title "WriteTheWeb", :uri nil, :entries [
			{:authors [], :categories [], :content nil, :contributors [], :description {:type "text/plain", :value "WorldOS is a framework on which to build programs..."}, :enclosures [], :link "http://writetheweb.com/read.php?item=24", :published-date nil, :title "Giving the world a pluggable Gnutella", :updated-date nil, :url nil, :uri "http://writetheweb.com/read.php?item=24"}
			{:authors [], :categories [], :content nil, :contributors [], :description {:type "text/plain", :value "After a period of dormancy, the Syndication mailing list..."}, :enclosures [], :link "http://writetheweb.com/read.php?item=23", :published-date nil, :title "Syndication discussions hot up", :updated-date nil, :url nil, :uri "http://writetheweb.com/read.php?item=23"}
			{:authors [], :categories [], :content nil, :contributors [], :description {:type "text/plain", :value "The Magi Project is an innovative project to create..."}, :enclosures [], :link "http://writetheweb.com/read.php?item=22", :published-date nil, :title "Personal web server integrates file sharing and messaging", :updated-date nil, :url nil, :uri "http://writetheweb.com/read.php?item=22"}]}})

(deftest test-feeds
	(doseq [[feed-type value] feeds]
		(testing feed-type
			(let [f (parse-feed (slurp (format "test/resources/%s.xml" feed-type)))]
				(is (.equals f value))))))

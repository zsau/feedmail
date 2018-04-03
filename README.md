# Feedmail

Feedmail fetches RSS/Atom feeds, then converts their entries to emails and uploads them to an IMAP account. This lets you check your feeds with any email client, and you get synchronization across devices for free. You'd normally run feedmail as a cron job.

## Config file

To use feedmail, give it your IMAP credentials and a list of feeds to fetch ("subscriptions") by modifying `config.clj`. The file lives in `~/.config/feedmail/` by default, but you can put it elsewhere with the `-c` command-line argument.

Feedmail is written in Clojure, and its config file uses Clojure syntax. If you don't know Clojure, the basic syntax is simple: `{:key1 "value", :key2 1234}` is a map (commas optional), and `[thing1 thing2]` is a list. This minimal config should hopefully be self-explanatory:

```clojure
{:imap {
   :host "imap.gmail.com"
   :user "nobody"
   :password "hunter2"}
 :subscriptions [
   {:url "http://example.com/rss.xml"
    :folder "Example Feed"}
   {:url "https://www.theonion.com/rss"
    :folder "Humor/The Onion"}]}
```

Here's a more comprehensive example, which assumes more Clojure knowledge. Keys not included in the above config are optional.

```clojure
{:imap {
   :host "imap.gmail.com"
   :user "nobody"
   :password "hunter2"
   :port 993}
 ;Feedmail uses a cache to remember which items it has seen before. You probably won't need to change these values.
 :cache {
   :path "/home/nobody/.cache/feedmail"
   :size 100}
 :email {
   ;This template should include at least one <a class='link'>, plus elements with classes `title` and `content`.
   :template "<h1><a class='link title'></a></h1><p class='content'></p>"}
 :subscriptions [
   {:url "https://news.ycombinator.com/rss"
    :folder "Job Hunting"
    ;include only items whose title includes the text "Who is hiring?"
    :filter #(.contains (:title %) "Who is hiring?")}
   {:url "http://nedroid.com/feed/"
    :folder "Comics/Nedroid"
    ;Override the author of each entry to be "Beartato"
    :map #(assoc % :author "Beartato")
    ;Squelches output from any errors encountered while fetching this feed. Useful for flaky feeds that generate too many cron emails.
    :suppress-errors true}
   ;URLs like `script:/path/to/script` tell feedparser to execute an external command, which should output RSS or Atom content. Useful for HTML-scraping wrappers that generate RSS feeds for sites that don't provide one, or even for generating feeds from sources other than websites.
   {:url "script:/home/nobody/my-rss-generator.py"
    :folder "Special"}]}
```

## Important note about config.clj

Feedparser *evals* the config file, allowing you to use Clojure code anywhere. This is powerful but dangerous: it's not sandboxed in any way, allowing arbitrary code execution. Because of that and the `script:` URL feature, **don't run feedmail with an untrusted config file!** Since your config will also contain your IMAP credentials, it's best to `chmod 600` your `config.clj`.

## License

Released under the [MIT License](https://opensource.org/licenses/MIT).

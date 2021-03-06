# Feedmail

Feedmail fetches RSS/Atom feeds, then converts their entries to emails and uploads them to an IMAP account. This lets you check your feeds with any email client, and you get synchronization across devices for free. You'd normally run feedmail as a cron job.

## Config file

To use feedmail, give it your IMAP credentials and a list of feeds to fetch ("subscriptions") by modifying `config.clj`. This file lives in `~/.config/feedmail/` by default, but you can put it elsewhere with the `-c` command-line argument.

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
 ;; Feedmail uses a cache to remember which items it has seen before.
 ;; You probably won't need to change these values.
 :cache {
   :path "/home/nobody/.cache/feedmail"
   :size 100}
 :email {
   ;; Your template should include at least one `<a class='link'>`,
   ;; plus elements with classes `title` and `content`.
   :template "<h1><a class='link title'></a></h1><p class='content'></p>"}
 :subscriptions [
   {:url "https://news.ycombinator.com/rss"
    :folder "Job Hunting"
    ;; Include only items whose title includes specific text:
    :filter #(.contains (:title %) "Who is hiring?")}
   {:url "http://nedroid.com/feed/"
    :folder "Comics/Nedroid"
    ;; Override the author of each entry:
    :map #(assoc % :author "Beartato")
    ;; Squelch output from errors encountered while fetching this feed.
    ;; Useful for flaky feeds that generate too many cron emails.
    :suppress-errors true}
   ;; URLs with the scheme `exec:` tell feedmail to execute a shell command,
   ;; which should output RSS or Atom content.
   ;; Use this to generate feeds from websites that don't provide one, or from other sources.
   ;; You must percent-escape non-URL-safe chars, inluding spaces between arguments!
   {:url "exec:twitter-to-rss%20richhickey"
    :folder "Clojure"}]}
```

## Important note about config.clj

Feedparser *evals* the config file, allowing you to use Clojure code anywhere. This is powerful but dangerous: it's not sandboxed in any way, allowing arbitrary code execution. Because of that and the `exec:` URL feature, **don't run feedmail with an untrusted config file!** Since your config will also contain your IMAP credentials, it's best to `chmod 600` your `config.clj`.

## License

Released under the [MIT License](https://opensource.org/licenses/MIT).

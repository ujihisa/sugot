(defproject sugot "1.1-SNAPSHOT"
  :description "clojure + spigot minecraft server"
  :url "https://github.com/ujihisa/sugot"
  :license {:name "GPL3 or any later versions"
            :url "http://www.gnu.org/licenses/gpl-3.0.en.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.spigotmc/spigot-api "1.9.4-R0.1-20160515.121902-8"]
                 [org.spigotmc/spigot "1.9.4"]
                 [clj-http "3.0.1"]]
  :repositories {"org.bukkit"
                 "http://repo.bukkit.org/content/groups/public/"
                 "spigot-repo"
                 "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"
                 "localrepo1"
                 "file://myrepo"}
  :profiles {:dev {:dependencies [[midje "1.8.3" :exclusions [org.clojure/clojure]]]
                   :plugins [[lein-midje "3.1.3"]]}}
  :main ^:skip-aot sugot.core
  :target-path "target/%s"
  :min-lein-version "2.5.0"
  :jvm-opts ["-XX:MaxPermSize=128M"]
  :plugins [[lein-cloverage "1.0.6"]])

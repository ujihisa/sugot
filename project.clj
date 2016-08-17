(defproject sugot "1.5"
  :description "clojure + spigot minecraft server"
  :url "https://github.com/ujihisa/sugot"
  :license {:name "GPL3 or any later versions"
            :url "http://www.gnu.org/licenses/gpl-3.0.en.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.spigotmc/spigot-api "1.10.2-R0.1-20160815.102659-55"]
                 [org.spigotmc/spigot "1.10.2"]
                 [clj-http "3.1.0"]]
  :repositories {"spigot-repo"
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

(defproject sugot "1.0-SNAPSHOT"
  :description "clojure + spigot minecraft server"
  :url "https://github.com/ujihisa/sugot"
  :license {:name "GPL3 or any later versions"
            :url "http://www.gnu.org/licenses/gpl-3.0.en.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.spigotmc/spigot-api "1.8.7-R0.1-SNAPSHOT"]
                 [org.bukkit/bukkit "1.8.7-R0.1-SNAPSHOT"]]
  :repositories {"org.bukkit"
                 "http://repo.bukkit.org/content/groups/public/"
                 "spigot-repo"
                 "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"
                 "localrepo1"
                 "file:myrepo"}
  :main ^:skip-aot sugot.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[org.spigotmc/spigot "1.8.7"]]}}
  :min-lein-version "2.5.0"
  :jvm-opts ["-XX:MaxPermSize=128M"])

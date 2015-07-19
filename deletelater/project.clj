(defproject deletelater "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 ^{:pom-scope :provided} [org.spigotmc/spigot-api "1.8.7-R0.1-SNAPSHOT"]
                 ^{:pom-scope :provided } [org.bukkit/bukkit "1.8.7-R0.1-SNAPSHOT"]]
  :repositories {"org.bukkit"
                 "http://repo.bukkit.org/content/groups/public/"
                 "spigot-repo"
                 "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"}
  :main ^:skip-aot deletelater.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

# sugot [![Build Status](http://jenkins.raa0121.info/job/sugot/badge/icon)](http://jenkins.raa0121.info/job/sugot/)

![sugot logo made by ujihisa](http://cache.gyazo.com/8b5b3fba0e9ea94b303acd77e8920a8c.png)

Clojure + Spigot minecraft server

## How to run the minecraft server

1. Prepare `spigot-1.9.4.jar` https://www.spigotmc.org/wiki/buildtools/
2. Install it with `lein deploy localrepo1 org.spigotmc/spigot 1.9.4 spigot-1.9.4.jar`
3. `lein run`

You can also `lein midje` to check if it works without starting a Minecraft server..

TODOs

* specify port

## Contributing

1. `lein cloverage` to run test and get test coverage. Make sure your code is covered.

### Coding Convention

* Don't abbreviate names for vars (e.g. prefer `player` to `p`) of your main concern
    * You can abbreviate them if they aren't the main concern of the function
* Avoid naming colision, and for that you can abbreviate. (e.g. prefer `fmt` to `format`)

### Tips

  * `lein midje :autotest` to run a process to keep running unit test
    when you modify a file.

## FAQ

* Do I have to `lein uberjar`?
    * No. Just `lein run`

## License

GPLv3 or any later versions

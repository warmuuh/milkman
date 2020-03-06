# Graal Native Image Build of Milkman

:exclamation: this is an experimental feature. it is not compatible with existing milkman installations (as persistence layer was changed) and might break it again in future versions of native release.

This package contains setup for building a native image of milkman, bundled with some plugins. 
Because of the [limitations](https://github.com/oracle/graal/blob/master/substratevm/LIMITATIONS.md) 
of graal, several features are a bit different / missing.

# Why
Startup time (as in `time until usable`) was always of high importance in Milkman. 
On my current machine, the normal startup time is around 2.2 seconds.
With [AppCds](https://openjdk.java.net/jeps/310) (which is automatically [setup on first start](https://cubiccow.blogspot.com/2019/07/fast-startup-in-milkman-using-appcds.html)), 
startup time is reduced to about 1.4 seconds.

When using the native build, startup time is around __0.6 seconds__.

This project also showcases already supported features of GraalVm Native-image compilation such as:

* Reflection support for predefined classes
* Dynamic Proxy support for predefined interfaces
* [SPI](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) support. (Milkman plugin mechanism is based on SPI)
* JavaFX support (via [Gluon Substrate](https://gluonhq.com/gluon-substrate-and-graalvm-native-image-with-javafx-support/))
* [Building native image](https://github.com/warmuuh/milkman/blob/feature/graal/.github/workflows/native.yml) via Github Actions


# Download

Currently, there is only a native image available for **macos**, built via Github Actions.
You can download latest native build from the latest ['native' workflow artifacts](https://github.com/warmuuh/milkman/actions?query=workflow%3Anative).

# Inlcuded plugins
No support for dynamically adding plugins besides the included ones.

* [Http Request Plugin](https://github.com/warmuuh/milkman/blob/master/milkman-rest) is included
* Others planned (explore, scripting (replacing nashorn with graal), privatebin, GraphQl, Note)
* [JDBC Plugin](https://github.com/warmuuh/milkman/blob/master/milkman-jdbc) not possible as it requires dynamically loaded database drivers

# Changes / Limitations

Due to the limitations of graal, several changes to milkman were necessary:

* Persistence: because [Nitrite](https://github.com/dizitart/nitrite-database) uses serialization, 
the persistence layer was exchanged and uses [MapDB](http://www.mapdb.org/). (Might not be the final solution though)
* No support for dynamically adding plugins besides the included ones.
* Clipboard (Copy & Paste) is not working ([issue](https://github.com/gluonhq/substrate/issues/227))
* No Windows support yet as Gluon Substrate has to catch up ([issue](https://github.com/gluonhq/substrate/issues/205))


# Build local
You have to [setup GraalVm](https://www.graalvm.org/docs/getting-started/), especially GRAALVM_HOME env variable and 
have native-image installed via `gu install native-image`

```
mvn package -Pnative -pl milkman,milkman-cli,milkman-rest,milkman-dist-graal
```

resulting native image is available under `./milkman-dist-graal/target/client/<arch>/milkman-dist-graal`

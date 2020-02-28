# Graal Native Image Build of Milkman

:exclamation: this is an experimental feature

This package contains setup for building a native image of milkman. 
Because of the [limitations](https://github.com/oracle/graal/blob/master/substratevm/LIMITATIONS.md) 
of graal, several features are a bit different / missing.

# Why
Startup time was always of high importance in Milkman. 
On my current machine, the normal startup time is around 2.2 seconds.
With [AppCds](https://openjdk.java.net/jeps/310) (which is automatically setup on first start), 
startup time is reduced to about 1.4 seconds.

When using the native build, startup time is around __0.6 seconds__.

# Changes

Due to the limitations of graal, several changes to milkman were necessary:

* Persistence: because [Nitrite](https://github.com/dizitart/nitrite-database) uses serialization, 
the persistence layer was exchanged and uses [MapDB](http://www.mapdb.org/).
* No support for dynamically adding plugins besides the included ones.
* Currently, only Rest-plugin is included.

# Build

```
mvn package -Pnative -pl milkman,milkman-cli,milkman-rest,milkman-dist-graal
```

resulting native image is available under `./milkman-dist-graal/target/client/<arch>/milkman-dist-graal`

# Notes

* No Windows support yet as Gluon Substrate has to catch up (https://github.com/gluonhq/substrate/issues/205)

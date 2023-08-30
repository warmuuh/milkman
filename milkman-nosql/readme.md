# Milkman Nosql Plugin

Introduces Nosql Requests to Milkman using [JNoSql](http://www.jnosql.org/).

# Installation
After placing the jar into `\plugin` folder, you also have to place the [driver-jars](https://github.com/eclipse/jnosql-databases) you want to use in that folder as well.

# Screenshot

TBD

# Features

* Integration of jnosql [query language](https://github.com/eclipse/jnosql/blob/main/COMMUNICATION.adoc#querying-by-text-with-the-communication-api)
* support for key-value databases (e.g. dynamodb, redis, memcache,...)
* (planned) support for document databases (e.g. couchbase, )
* (planned) support for column databases (e.g. cassandra, )
* (planned) support for graph databases (via Apache tinkerpop, [supporting](https://tinkerpop.apache.org/providers.html) databases like neo4j)
* (planned) exploration of table structures / existing tables.

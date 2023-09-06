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


# Setup examples

## DynamoDb

you need to add these artifacts into the plugin folder:

```yaml
org.eclipse.jnosql.databases:jnosql-dynamodb:1.0.1
jakarta.json.bind:jakarta.json.bind-api:3.0.0
org.eclipse:yasson:3.0.3
org.glassfish:jakarta.json:2.0.1

# if you use `aws sso login` 
software.amazon.awssdk:sso:2.20.98
```

for a nosql request, you need to configure these parameters:
```properties
jnosql.keyvalue.provider=org.eclipse.jnosql.databases.dynamodb.communication.DynamoDBKeyValueConfiguration
jnosql.keyvalue.database=your-db-name
jnosql.dynamodb.region=...
jnosql.dynamodb.profile=...
```

*limitation*: jnosql dynamodb can only use string-values

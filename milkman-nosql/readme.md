# Milkman Nosql Plugin

Introduces Nosql Requests to Milkman using [JNoSql](http://www.jnosql.org/).

# Installation
After placing the jar into `\plugin` folder, you also have to place the [driver-jars](https://github.com/eclipse/jnosql-databases) you want to use in that folder as well.

# Screenshot

TBD

# Features

* Integration of jnosql [query language](https://github.com/eclipse/jnosql/blob/main/COMMUNICATION.adoc#querying-by-text-with-the-communication-api)
* support for key-value databases (e.g. dynamodb, redis, memcache,...)
* support for document databases (e.g. couchbase, )
* support for column databases (e.g. cassandra, )
* (planned) support for graph databases (via Apache tinkerpop, [supporting](https://tinkerpop.apache.org/providers.html) databases like neo4j)
* (planned) exploration of table structures / existing tables.


# Setup examples

## DynamoDb

you need to add these artifacts into the plugin folder (refering to my [dynamodb jnosql driver](https://github.com/warmuuh/jnosql-dynamodb-document))

```yaml
com.github.warmuuh:jnosql-dynamodb-document:1.0.1-SNAPSHOT


# if you use `aws sso login` 
software.amazon.awssdk:sso:2.20.98
```

for a nosql request, you need to configure these parameters:
```properties
jnosql.document.provider: com.github.warmuuh.jnosql.dynamodb.DynamoDBDocumentConfiguration
jnosql.dynamodb.region: eu-central-1
jnosql.dynamodb.profile: my_profile
```


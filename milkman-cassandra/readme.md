# Milkman Cassandra Plugin

Introduces Cql Requests to Milkman using cassandra datastax driver.

## Requirements
This plugin requires `milkman-jdbc` plugin

## Installation
After placing the jar into `\plugin` folder, you also have to place the driver-jars you want to use in that folder as well.

## Usage

The used url is of following format:
```
cql://host[/keyspace]?dc=...[&username=...&password=...]
```

supported parameters

| name | description |
| --- | --- |
| dc | datacenter, required. for local installations, this should be 'datacenter1' |
| username | username, optional |
| password | password, optional |

## Screenshot

![img](/img/cassandra-plugin.png)

## Features

 * Execution of Requests against cassandra databases

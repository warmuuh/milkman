# Milkman GRPC plugin

Grpc Plugin for communication with Grpc Servers.

## Features

  * Can work with [Server Reflection](https://github.com/grpc/grpc/blob/master/doc/server-reflection.md) to query services and retrieve *.proto files
  * Given a *.proto file, Server Reflection is not necessary to query a service
  * Read/write ASCII headers
  * Support Server/client/both streams
  
  
## Screenshot

![Milkman Grpc](/img/grpc-plugin.png)

### Example of Server streaming

![Milkman Grpc Streaming](/img/gif/grpc-streaming.gif)

### Client Streaming

To send multiple messages, just add multiple json objects to the payload, divided by two new lines.
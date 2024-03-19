# Scripting API

The following section describes how to access various properties of the request/response in 
pre or post scripts.

* `Flux` type is usually accessible in blocking way as plain string.

## Api Reference

### Request Type: GraphQl

#### Request properties

* Aspect `graphql` (GraphqlAspect)
  * name (String)
  * query (String)
  * variables (String)
#### Response properties


### Request Type: Grpc

#### Request properties

* Aspect `headers` (GrpcHeaderAspect)
  * entries (List)
  * name (String)
* Aspect `payload` (GrpcPayloadAspect)
  * name (String)
  * payload (String)
* Aspect `operation` (GrpcOperationAspect)
  * name (String)
  * operation (String)
  * protoSchema (String)
  * useReflection (boolean)
#### Response properties

* Aspect `headers` (GrpcResponseHeaderAspect)
  * entries (CompletableFuture)
  * name (String)
* Aspect `Payload` (GrpcResponsePayloadAspect)
  * name (String)
  * payloads (Flux)

### Request Type: HTTP

#### Request properties

* Aspect `headers` (RestHeaderAspect)
  * entries (List)
  * name (String)
* Aspect `body` (RestBodyAspect)
  * body (String)
  * name (String)
* Aspect `query` (RestQueryParamAspect)
  * entries (List)
  * name (String)
#### Response properties

* Aspect `headers` (RestResponseHeaderAspect)
  * entries (List)
  * name (String)
* Aspect `debugBody` (DebugRequestBodyAspect)
  * body (String)
  * name (String)
* Aspect `debugHeaders` (DebugRequestHeaderAspect)
  * entries (List)
  * name (String)
* Aspect `body` (RestResponseBodyAspect)
  * body (Flux)
  * name (String)

### Request Type: NoSql

#### Request properties

* Aspect `query` (NosqlQueryAspect)
  * name (String)
  * query (String)
* Aspect `parameters` (NosqlParameterAspect)
  * entries (List)
  * name (String)
#### Response properties

* Aspect `result` (NosqlResponseAspect)
  * columnNames (List)
  * name (String)
  * rows (List)

### Request Type: SQL

#### Request properties

* Aspect `sql` (JdbcSqlAspect)
  * name (String)
  * sql (String)
#### Response properties

* Aspect `result` (RowSetResponseAspect)
  * columnNames (List)
  * name (String)
  * rows (List)

### Request Type: Socket.IO

#### Request properties

* Aspect `sio` (SocketIOAspect)
  * event (String)
  * message (String)
  * name (String)
* Aspect `settings` (SocketIoSettingsAspect)
  * clientVersion (SocketIoVersion)
  * handshakePath (String)
  * name (String)
#### Response properties

* Aspect `sioBody` (SocketIOResponseAspect)
  * client (MilkmanSocketIOClient)
  * name (String)

### Request Type: TEST

#### Request properties

* Aspect `test` (TestAspect)
  * environmentOverride (List)
  * name (String)
  * propagateResultEnvironment (boolean)
  * requests (List)
  * stopOnFirstFailure (boolean)
#### Response properties

* Aspect `Environment` (TestResultEnvAspect)
  * environment (Environment)
  * name (String)
* Aspect `Results` (TestResultAspect)
  * name (String)
  * results (Flux)

### Request Type: WebSocket

#### Request properties

* Aspect `ws` (WebsocketAspect)
  * message (String)
  * name (String)
#### Response properties

* Aspect `wsBody` (WebsocketResponseAspect)
  * client (MilkmanWebsocketClient)
  * name (String)

### Request Type: defined at `JqlAspectPlugin`

#### Request properties

* Aspect `jql` (JqlQueryAspect)
  * name (String)
  * query (String)
  * queryHistory (List)
#### Response properties


### Request Type: defined at `NotesAspectPlugin`

#### Request properties

* Aspect `note` (NotesAspect)
  * name (String)
  * note (String)
#### Response properties


### Request Type: defined at `ScriptingAspectPlugin`

#### Request properties

* Aspect `script` (ScriptingAspect)
  * name (String)
  * postRequestScript (String)
  * preRequestScript (String)
  * preScriptOutput (String)
#### Response properties

* Aspect `Script Output` (ScriptingOutputAspect)
  * name (String)
  * postScriptOutput (String)
  * preScriptOutput (String)

### Request Type: unknown

#### Request properties

* Aspect `unknown` (UnknownRequestAspect)
  * content (TreeNode)
  * name (String)
#### Response properties



# Milkman Rest Plugin

Introduces Http request (despite the wrong naming) capabilities to milkman. Consists of serveral features that together should make milkman be usable as postman-replacement for day-to-day work.

# Screenshot

![Milkman Http](/img/screenshot.png)

### Example of Server Sent Events Streaming

![Milkman SSE Streaming](/img/gif/sse-streaming.gif)

# Features

 * Postman-like UI
 * Crafting of requests by editing body, headers, parameters
 * Highlighting/formatting for json
 * Proxy-Authentication support (BASIC for now)
 * Importers for Postman exports (Collections, Environments, Data-Dump)
 * Importers for OpenApi v3.0
 * (planned) Exporters
 * Support import of APIs listed at [APIs.guru](https://apis.guru/), see [demo](/img/gif/milkman-library.gif)
 * http/2, http/3 support (see options-dialog)

## Client-Certificates 

You can import client-certificates that will be used when the server asks. Just add the according PEM files in the `HTTP` options:

![Milkman Rest Client Certificate Options](/img/client-cert-option.png)

and then, you can choose the client-certificate in the options of the request:

![Choosing Client Certificate](/img/choose-client-certificate.png)
# Milkman Scripting plugin

provides post-request script execution via `nashorn` javascript engine.

# Example

```javascript
var url = JSON.parse(milkman.response.body.body).url
milkman.setEnvironmentVariable("test.url", url)
```

# Screenshot

![img](/img/scripting-plugin.png)

# API

`milkman` is the only object available. It provides following properties:

 * `respone`: an accessor for response-aspects. Every property of this response-object tries to fetch an according aspect. E.g. `response.body` would be the body-aspect of the response-container. (Thats why you have to write `response.body.body`, because the body-aspect stores its content in the `body` variable as well. Might be streamlined in the future)
 *  `setEnvironmentVariable(String, String)` sets an environment variable in the currently active environment (if there is any activated).




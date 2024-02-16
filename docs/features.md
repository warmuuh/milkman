# Features

## Marketplace

There is a marketplace where plugins are shown that are released on github.
Instructions on how to publish your own plugins can be seen [here](/docs/plugin-development.md).

![Marketplace](/img/marketplace.png)

## Code Folding

* Folding is supported in response body area
* Toolbar actions: expand all, collapse all, expand one level, collapse one level
* Clicking on the line-symbol expands the node
* Right-Clicking on the line-symbol expands the whole subtree

![folding-video](/img/gif/folding.gif)


## Hotkeys

  * <kbd>CTRL</kbd>+<kbd>ENTER</kbd> - Execute Request
  * <kbd>CTRL</kbd>+<kbd>N</kbd> - New Request
  * <kbd>CTRL</kbd>+<kbd>R</kbd> - Rename Active Request
  * <kbd>CTRL</kbd>+<kbd>W</kbd> - Close Active Request
  * <kbd>CTRL</kbd>+<kbd>S</kbd> - Save Active Request
  * <kbd>CTRL</kbd>+<kbd>E</kbd> - Edit current Environment
  * <kbd>CTRL</kbd>+<kbd>Space</kbd> - Quick-Edit of Variables
  * <kbd>ESC</kbd> - Cancel running Request
  

## Copy&Paste in Tables

* You can <kbd>CTRL</kbd>+<kbd>C</kbd> selected rows to copy its value
* You can <kbd>CTRL</kbd>+<kbd>V</kbd> multiple rows into a table

![copypaste-video](/img/gif/copypaste.gif)

## Quick Edit for Variables

* You can highlight variables.
* Clicking on it opens popup for modification/creation
* <kbd>ESC</kbd> hides highlighting

![highlightvars-video](/img/gif/hightlight-vars.gif)

## Keys

* secret keys that should not be exported or synced can be setup using the key-symbol
* currently, secret keys are only plain type keys, but will be e.g. oauth-keys etc
* can be accessed using {{key:name-of-key}} variable


# Libraries

you can register libraries to easily look-up and import services from a central registry, such as [APIs.guru](http://apis.guru).

![Example of setting up and using Libraries](/img/gif/milkman-library.gif)


## Code Templates

* you can define custom templates for every type of request yourself. Do you often need to write Spring Webclient code?
setup some custom template for it. (you can do so in the `options` dialog)
* Some predefined templates are available already. (see [here](/milkman-rest/src/main/resources/META-INF))
* Syntax is similar to [mustache](https://github.com/samskivert/jmustache) but enhanced with whitespace control. example:

```
curl -X {{httpMethod}}  

{{_#headers.entries-}}
-H "{{name}}: {{value}}"
{{-/headers.entries_}}

{{url}}
```

* Whitespace control: 
    * `{{-` removes all whitespaces (including linebreaks) *before* the tag
    * `{{_` removes all whitespaces (including linebreaks) *before* the tag and replaces it with one space
    * `-}}` removes all whitespaces (including linebreaks) *after* the tag
    * `_}}` removes all whitespaces (including linebreaks) *after* the tag  and replaces it with one space


## Insomnia Import

* exported Collections (see [Insomnia Documentation](https://docs.insomnia.rest/insomnia/import-export-data#export-data)) can be imported into Milkman.
* currently, Http, Websocket, event-stream, Grpc and Gql requests are supported
* environments are also imported
* :exclamation: There are some conceptual differences between insomnia and milkman though:
    * Insomnia root-level requests are imported into a collection with the name of the workspace
    * Insomnia environments overload each other based on a hierarchy. this is flattened on import.
    * Grpc requests refere to a shared file in the workspace. This is flattened in milkman (i.e. file-content is copied to each request refering to it)

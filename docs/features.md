# Features



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


## Custom Templates

* you can define custom templates for every type of request yourself. Do you often need to write Spring Webclient code?
setup some custom template for it. (you can do so in the `options` dialog)
* Some predefined templates are available already.
* Syntax is similar to mustache but enhanced with whitespace control. example:
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

# Changelog

version 5.6.0 -- upcoming (nightly)
  * custom export templates (see [Features](/docs/features.md))
  * some predefined exporters for
    * Spring RestTemplate

version 5.5.0

  * added new [Socket.io plugin](/milkman-sio) (thanks to @pauljakals)
  * added importer for libraries such as [APIs.guru](http://apis.guru) or others via plugins

version 5.4.0
  * added debug output to see what got send (http only for now)
  * store customized table-column widths
  * added option to not export keys on workspace export
  * added option to repeat a test n times
  * fix: ignore disabled headers in http exporter
  * added Drag&Drop for folders
  * added colors/icons to request types and status information (can be disabled in options)
  * disabled javafx off-color subpixel antialiasing for font (macOs)
  * openapi import: importing server-urls as environment variables
  * added colors to environments

version 5.3.0
  * mask plain-keys on overview
  * timeouts for oauth
  * auto-refresh for oauth only on usage (not on preview)
  * oauth client-credential transmission via header or form parameter now
  * auto-completion now includes prefixed resolvers (such as `key:`)
  * new [WebSocket plugin](/milkman-ws)

version 5.2.0
  * added key-store that is not synced
  * added oauth2 support ([Auth Plugin](/milkman-auth)) (client-credential, password grant, authorization code (w/o fixed redirect-url))

version 5.1.0
  * added horizontal layout switch
  * added result environment to test plugin
  * fixed bugs in test-plugin that lead to re-execution of tests
  * added support for line-breaks in `x-www-form-urlencoded` mimetype requests
  * improved dirty-indicator for request tabs
  * added Grpc Tls support
  * refactored theming (using scss now), added new `trump` theme
  * added read-only sync mode to only pull changes

version 5.0.0
  * included [cassandra plugin](/milkman-cassandra) and [Grpc plugin](/milkman-grpc) into macos app bundle
  * reordered toolbar-actions
  * added image-preview for responses of picture-mimetype
  * added new plugin [testing plugin](/milkman-test) for running test suites
  * added `duplicate request` to tab context menu
  * added cache for cassandra-connections, so subsequent calls are faster
  * fixing multipart requests to use CRLF in body #76
  * improved dark theme
  
version 4.6.0
  * added option to disable ui animations
  * added cqlsh export for cassandra
  * added curl, http export for graphql
  * added [grpcurl](https://github.com/fullstorydev/grpcurl) export
  * added full resolve for privatebin export
  * fixed bug in grpc endpoint field
  * improved grpc service discovery command

version 4.5.0
  * added [cassandra plugin](/milkman-cassandra)
  * added curl import 
  * fixing grpc plugin on macos
  * added http export

version 4.4.0
  * added openApi v3.0 yaml importer
  * support for pluggable environment resolvers + script support
    * things like `{{js:base64("test:me"}}` is supported now, see [scripting plugin](/milkman-scripting) for more information
  * added support for pre-request scripts
  * added support for pre-loading scripts before executing pre/post request scripts (e.g. load chai before asserting things)
  * added output method for scripts via console.log / print (logging)

version 4.3.0 
  * several UX enhancements / fixes
  * made search case-insensitive
  * reworked "save as.." dialog.
    * support folders
    * more intelligent matching of collections, using given chars to match, not necessarily substring
  * improved behavior of code editor
    * auto-indent based on content (json supported for now)
    * Tabs as spaces
    * outdenting supported
  * added quick-view&edit of template parameters (Ctrl-Space) (see [Features](/docs/features.md))
  * added popup area for editing long urls
  * fixed proxy authentification, added realm info
  
version 4.2.0 
  * removed FXML dependency (small performance improvement, preparation for native image)
  * added support for TLS1.0. #41
  * setup build and release process via github actions

version 4.1.0 
  * added import/export of whole workspace
  * fixed crash caused by disclosure node in tree view
  * fixed access of request body via script plugin


version 4.0.0
  * added [Grpc plugin](/milkman-grpc)
  * changed way of proxy authentication for http2 client to be less rigid
  * made Milkman asynchronous at core, for better support of streaming response types, such as Grpc Streaming, SSE or WebSockets
  * added chunking support for [http plugin](milkman-rest), enabling Server-sent events (SSE) testing.

version 3.7.1
  * added experimental Http/2 support (see options)
  * added macos app bundling added
  * fixed small bugs in environment variable dialog
  * fixed git-sync for unwritable currentdir
  * fixed macos icon in taskbar
  
version 3.6.0
  * added Commandline Interface `mm` for milkman
  * added jdbc `Show Table Schema` command
  * improved handling of big unformatted responses
  * improved search in content  
  * support of nested folders in postman import
  

version 3.5.0 
  * improved tabbing in tables (only editable cells), adds new entry on <kbd>TAB</kbd> in last editable cell
  * new graphql plugin
  * added quick selection of new requests, if multiple providers exist
  * disabled some animations due to performance reasons
  
version 3.4.0
  * fixed github sync initialization
  * fixed github sync issue (Reflections lib incompatible with java9)
  * fixed auto-formatting
  * updated priatebin integration to new v1.3
  * using client vm for less memory
  
version 3.3.0
  * code folding for application/json
  * fixed postman collection v2.1 import bug
  * improved exploration plugin with history
  * headers can now be copied from response and pasted into request section
  * improved startup speed via [AppCds](https://blog.codefx.org/java/application-class-data-sharing/)

version 3.2.1
  * added option to not follow redirects
  * sticky response-tabs (dont reset to first tab anymore)
  * fixed issues with duplicate environment variable keys
  * enhanced macos/linux artifacts (executable scripts, install.sh for linux, lineendings)
  * use os-specific appdata folder, if current folder is not writable

version 3.2.0 
  * Performance improvements: binary css, replaced fxml with java
  * minor fixes for exceptions
  * fixed bug for adding headers
  * switched to xml formatter for text/xml format
  * fixed jdbc plugin order
  * added sub-folders for collections

version 3.1.0
  * nashorn, java security added to JRE
  * bugfix for collection view sorting
  * bugfix for autocompletion

version 3.0.0
  * JDK11 bundled distribution
  * several adjustments to adjust for java11
 
version 2.2.2
  * option to disable SSL verification
  * plugins can now use password fields in option
  

version 2.2.1
  * added Drag&Drop support for requests
  * improved auto completion
  * 'copy as csv' for jdbc results
  * added transpose function for jdbc results


version 2.2.0
  * generic postman import for dumps, collections (v1, v2.1), environments
  * postman export
  * enabled ssh credentials for git sync
    * jsch replaced with sshd for jgit (to support new ed25519 keys)
  * added custom commands for plugins
    * SHOW TABLES command for jdbc plugin


version 2.1.5:
  * bug: postman dump import broke github sync
  * UX: Clipboard support for table values (i.e. ctrl-c for headers and sql results)
  * UX: auto-resizing of table columns 
  * added action to edit current environment directly
  * auto completion for env variables, headers

version 2.1.4:
  * Shares via PrivateBin now viewable via browser 
  * Default to UTF-8 for responses
  * error log
  * proxy exclusion list
  * update check



version 2.1.3:
  * perf: highlighting now asynchronous and optimized
  * perf: startup speed improved
  * requests can now be cancelled
  * UX: response header values can now be selected&copied
  * UX: collections are now sorted by name & search filters request inside collections as well
  * UX: starring pops collections to top
  * UX: "Close Others" added
  * XML highlighter/formatter added
 
version 2.1.2:
  * bug: request type needs order (for defaulting)
  * bug: request aspect tab headers captions are white
  * bug: request aspect tab order is wrong?
  * bug: default workspace sync should be none (order!)
  * bug: NPE on "epoints/get all events"
  * bug: already-escaped url parameters are espaced again



version 2.1.1:
  * UX: click on already selected collection doesn't do anything
  * UX: selection colour in content editor
  * UX: middle mb on request tab should close



version 2.1
  * Synchronisation of environment
  * Export of requests / code (curl)
  * Export of collections (privatebin)
  * BUG: clicking on one specific request does not refresh working area, only, if headers-tab is active though (due to content-tab having a scrollbar => something breaks?!)



Version 2.0
  * SQL plugin
  * Starring collections
  * Team Sync via Github
  * bug: newly created workspace not showing in manageWorkspaceDialog
  * bug: request-tabs are not refreshed on creating new request / opening
  * UX: Convenience methods (closing all, etc)
  * UX: don't close dialogs on click outside
  * SQL formatter casing
  * bug: dirty=true ends up in persistence
  * UX: mnemonics, submit/cancel for dialogs with enter/esc
  * UX: working area: splitpane to change aspect ratio between request/response
  * ordering of aspect-tabs via plugins (default to body for rest)
  * Executable for windows

Version 1.1
  * Search in content
  * JavaScript runner for post request actions
  * themes (code / milkman)
  * Jmespath
  * Query Param editor
  * Icons for requests
  * UX: too long table column values (truncate)
  * UX: bigger dialogs
  * Dedupe via reactfx


Version 1.0
  * Update dirty state
  * Management dialogs nice
  * Validation in save as
  * Icons
  * Readme
  * postman dump import
  * bug: url escaping
  * Notes as sample plugin +docs






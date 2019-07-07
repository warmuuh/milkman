 # Planned
 
  * Export of environment (privatebin)
  * jdbc -> explorer functionality (what tables are there? what columns does a table have?)
  * jdbc -> lazy list loading
  * Use hardwire in plugins
  * javascript runner for pre
  * UX: Long URL editor
  * UX: highlighting of environment variables
  * UX: closing request confirmation

# Changelog

version 3.2.0 - upcomming
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






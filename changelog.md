Version 1.0
  * Update dirty state
  * Management dialogs nice
  * Validation in save as
  * Icons
  * Readme
  * postman dump import
  * bug: url escaping
  * Notes as sample plugin +docs

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

version 2.1
  * Synchronisation of environment
  * Export of requests / code (curl)
  * Export of collections (privatebin)
  * BUG: clicking on one specific request does not refresh working area, only, if headers-tab is active though (due to content-tab having a scrollbar => something breaks?!)

version 2.1.1:
  * UX: click on already selected collection doesn't do anything
  * UX: selection colour in content editor
  * UX: middle mb on request tab should close

version 2.1.2:
  * bug: request type needs order (for defaulting)
  * bug: request aspect tab headers captions are white
  * bug: request aspect tab order is wrong?
  * bug: default workspace sync should be none (order!)
  * bug: NPE on "epoints/get all events"
  * bug: already-escaped url parameters are espaced again

version 2.1.3:
  * perf: highlighting now asynchronous and optimized
  * perf: startup speed improved
  * requests can now be cancelled
  * UX: response header values can now be selected&copied
  * UX: collections are now sorted by name & search filters request inside collections as well
  * UX: starring pops collections to top
  * UX: "Close Others" added
  * XML highlighter/formatter added
  
version 2.2:
  * Shares via PrivateBin now viewable via browser 

Planned:
  * log-file 
  * Export of environment (privatebin)
  * Exports for postman
  * GitHub update check
  * jdbc -> explorer functionality (what tables are there? what columns does a table have?)
  * Use hardwire in plugins
  * javascript runner for pre
  * UX: Long URL editor
  * UX: highlighting of environment variables
  * UX: closing request confirmation






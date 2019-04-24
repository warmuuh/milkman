Plugins in milkman can extend various functionality. For this, an explanation of how a request is structured is necessary first.

# Getting Started
A [sample plugin](https://github.com/warmuuh/milkman/tree/master/milkman-note) was created that shows how to add an Aspect Tab to a Request.

if you want to setup a new project, an exemplary pom can be found [here](/docs/plugin-development-setup.md).

# Data Model

![img](http://www.gravizo.com/svg?@startuml;object%20Workspace;object%20Environment;Environment%20:%20isGlobal;Workspace%20o--%20%22*%22%20Environment;object%20Collection;Workspace%20o--%20%22*%22%20Collection;object%20Request;Collection%20o--%20%22*%22%20Request;%20Request%20--%3E%20HtmlRequest%20;%20Request%20--%3E%20SqlRequest%20;Request%20o-%20%22*%22%20RequestAspect;RequestAspect%20--%3E%20HttpHeaderRequestAspect;RequestAspect%20--%3E%20HttpBodyRequestAspect;@enduml)

The core of milkman is very abstract and is only intended to organize workspaces, which contain environments and collections of requests.
A request is of a specific type and might contain some basic data. In the case of an HttpRequest, this might be the URL and the Method.

A request can also contain several `RequestAspects` which describe the request object further. In our example, this might be headers or the body of a request, but can also contain totally unrelated and auxiliary attributes.

All Aspects and the container gets serialized using Jackson and stored in a local Database.

# Extension Points

Milkman uses [SPI](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html) for extension. You just have to provide an implementation to one of the Extension Points below and move your packaged jar into the `/plugin` folder to have milkman pick up your plugin.

## RequestAspectsPlugin

A request aspect can add aspects to either a request- or response container as well as according editors (providing the Tab to edit this specific aspect).

## ContentTypePlugin

A content type plugin is used to format and highlight content based on a mime-type.

## RequestTypePlugin

A plugin providing a request type such as HttpRequest, or SQL request or whatever you can think of.
This plugin has to provide a small editor for basic attributes of the request as well.

## ImporterPlugin

a plugin that imports things into the current workspace, such as collections, requests, environments.

## OptionPageProvider

a plugin to provide a UI for editing options of a "logical" plugin. The OptionPageBuilder can be used to create common ui. On startup, changed options will be loaded from database.

## UI Theme Plugin

provides an application-theme css and a syntax-theme css for styling.

## Workspace Synchronizer Plugin

provides a mechanism to synchronize the workspace with some external mechanism

## Request Export Plugin

extension point for adding export methods to a request-type.

# Persistence
All requests and RequestAspects (not response-aspects) will be stored in database and serialized using jackson. So you have to make sure that your classes properly serialize/deserialize.

# Common Components
Some common components are provided by milkman to make development of plugins easier:

 * TableEditor: a table that might or might not be editable. used for editing headers or environments or such.
 * ContentEditor: a content editor that supports highlighting and formatting
 * Dialogs: some common dialogs, such as credentialsInput or StringInput.

# Testing
milkman uses TestFX for testing. A [sample test](https://github.com/warmuuh/milkman/blob/master/milkman-note/src/test/java/milkman/plugin/note/NotesAspectEditorTest.java) can be seen in the notes plugin.

# Gotchas
JavaFX uses a lot of weak references. That means, if you dont keep references to e.g. bindings or controllers even (if they are not referred to by e.g. FXML-onActions), they get garbage-collected and the bindings simply dont work.
You can use `setUserData` in some cases to have a strong reference of the UI element to e.g. the controller, so they both get garbage-collected at the same time.

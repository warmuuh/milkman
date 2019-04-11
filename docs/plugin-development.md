Plugins in milkman can extend various functionality. For this, an explanation of how a request is structured is necessary first.

# Getting Started
A [sample plugin](https://github.com/warmuuh/milkman/tree/master/milkman-note) was created that shows how to add an Aspect Tab to a Request.

if you want to setup a new project, an exemplary pom can be found [here](/docs/plugin-development-setup.md).

# Data Model

A request is of a specific type and might contain some basic data. In the case of an HttpRequest, this might be the URL and the Method.

A request can also contain several `RequestAspects` which describe the request object further. In our example, this might be headers or the body of a request, but can also contain totally unrelated and auxiliary attributes.

All Aspects and the container gets serialized using Jackson and stored in a local Database.

# Extension Points

## RequestAspectsPlugin

A request aspect can add aspects to either a request- or response container as well as according editors (providing the Tab to edit this specific aspect).

## ContentTypePlugin

a content type plugin is used to format and highlight content based on a mime-type.

## RequestTypePlugin

a plugin providing a request type such as HttpRequest, or SQL request or whatever you can think of.
This plugin has to provide a small editor for basic attributes of the request as well. 

## ImporterPlugin

a plugin that imports things into the current workspace, such as collections, requests, environments.

## OptionPageProvider

a plugin to provide a UI for editing options of a "logical" plugin. The OptionPageBuilder can be used to create common ui.

# Common Components
 * TableEditor: a table that might or might not be editable. used for editing headers or environments or such.
 * ContentEditor: a content editor that supports highlighting and formatting
 * Dialogs: some common dialogs, such as credentialsInput or StringInput.

# Testing
milkman uses TestFX for testing. A [sample test](https://github.com/warmuuh/milkman/blob/master/milkman-note/src/test/java/milkman/plugin/note/NotesAspectEditorTest.java) can be seen in the notes plugin.

# Gotchas
JavaFX uses a lot of weak references. That means, if you dont keep references to e.g. bindings or controllers even (if they are not referred to by e.g. FXML-onActions), they get garbage-collected and the bindings simply dont work.
You can use `setUserData` in some cases to have a strong reference of the UI element to e.g. the controller, so they both get garbage-collected at the same time.

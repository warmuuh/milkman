# Milkman Commandline Interface

This is an experimental interactive command line interface for plugin. After copying the content of the archive to the root directory of milkman,
you can start it by `mm` on terminal.

# Screenshot

![img](/img/gif/cli.gif)


# Features

* Editing Request-Aspects via `nano`
* Executing requests
* Analyzing responses via `less`
* Interactive mode (entered by simply starting `mm`) or non-interactive mode (by executing commands via `mm [command]`)
* Tab-Completion on interactive mode

# Commands

* Note: all IDs are derived from the original names. All lowercase and special characters are replaced with `-`. E.g. `Your Workspace` becomes `your-workspace`*

| Command | Alias | Description | Arguments |
| ------ | --- |  -------  | ------ |
| change-workspace | ws | Switches currently activated workspace | `workspace` the id of the workspace to switch to | 
| change-collection | col | Switches currently activated collection | `collection`   the id of the collection to switch to |
| execute-request | req | Executes a given request | `requestish`<sup>1</sup>     the id of the request to execute<br>`-l`, `--less`      outputs response into less<br>`-v`, `--verbose`   outputs all aspects |
| edit-request | e | Edits an aspect of a request | `requestish`<sup>1</sup>   the id of the request to execute<br>`aspect`    the aspect to edit|
| quit | q | Quits Application | |

<sup>1</sup>A requestish is [[Workspace-Id/]Collection-Id/]Request-Id (i.e. the first two are optional)
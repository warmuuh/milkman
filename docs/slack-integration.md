# Slack Integration

Milkman supports slack-integration via a [slackbot](https://github.com/warmuuh/milkman-slack).
You can either host it yourself or use the heroku-hosted instance:

[![Add to Slack](https://platform.slack-edge.com/img/add_to_slack.png)](https://milkman-slack.herokuapp.com/slack/oauth/start)

# Usage

Once added to your workspace, you can share a private-bin export url via:

```
/milkman <privatebin-url>
```

# Features

It will nicely render the shared request and offers several ways for viewers to use the request, 
such as viewing the request as `curl`-command or `http`-request.

![Slack Preview](/img/slack-preview.png)

and users can choose how to view the request:

![Slack Render](/img/slack-render.png)


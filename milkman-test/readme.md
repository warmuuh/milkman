# Milkman Test Plugin

a testrunner plugin that allows to execute multiple requests in a sequence and verify their results.

## Usage

Just Drag-and-drop requests you want to execute into the test-tab and you are ready to go.

## Demo

![Milkman Test Demo](/img/gif/test-plugin-demo.gif)


## Environment

On execution, a separate environment only for this test-run will be created, so 
everything written to the current environment. For differentiation in scripts, a `__TEST__ = true` variable is also
added to the current environment.

Example test that is only executed on test-runs:
```javascript
if (mm.getEnvironmentVariable("__TEST__")){
  chai.should();
  var json = JSON.parse(mm.response.body.body)
  json.should.have.property('name').that.is.equal("Leanne Graham")
}
```

## Remark

This plugin is still in early development, so it might not seem to contain a lot of features. They will be added on-the-go when the need / issue arises.

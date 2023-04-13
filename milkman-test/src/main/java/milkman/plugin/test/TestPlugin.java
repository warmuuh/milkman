package milkman.plugin.test;

import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;
import milkman.plugin.test.domain.*;
import milkman.plugin.test.editor.*;
import milkman.ui.plugin.*;
import milkman.utils.AsyncResponseControl.AsyncControl;

import java.util.List;

public class TestPlugin implements RequestAspectsPlugin, RequestTypePlugin, RequestExecutorAware {

    private PluginRequestExecutor requestExecutor;

    @Override
    public List<RequestAspectEditor> getRequestTabs() {
        return List.of(new TestAspectEditor(), new TestAspectScenarioEditor(), new TestEnvironmentOverrideEditor());
    }

    @Override
    public List<ResponseAspectEditor> getResponseTabs() {
        return List.of(new TestResponseAspectEditor(), new TestResponseEnvironmentEditor());
    }

    @Override
    public void initializeRequestAspects(RequestContainer request) {
    }

    @Override
    public void initializeResponseAspects(RequestContainer request, ResponseContainer response, RequestExecutionContext context) {
        if (!(response instanceof TestResultContainer)){
            return;
        }
        var testAspect = request.getAspect(TestAspect.class).get();
        var testResultEnv = response.getAspect(TestResultEnvAspect.class).get();
        var testResultAspect = response.getAspect(TestResultAspect.class).get();
        testResultAspect.getResults().doOnComplete(() -> {
            if (testAspect.isPropagateResultEnvironment()){
                context.getActiveEnvironment().ifPresent(activeEnvironment -> {
                    testResultEnv.getEnvironment().getEntries().forEach(entry -> {
                        if (!entry.getName().equals("__TEST__")){
                            activeEnvironment.setOrAdd(entry.getName(), entry.getValue());
                        }
                    });
                });
            }
        }).subscribe();

    }

    @Override
    public int getOrder() {
        return 30;
    }


    @Override
    public RequestContainer createNewRequest() {
        var container = new TestContainer("New Test");
        container.addAspect(new TestAspect());
        return container;
    }

    @Override
    public RequestTypeEditor getRequestEditor() {
        return new TestContainerEditor();
    }

    @Override
    public ResponseContainer executeRequest(RequestContainer request, Templater templater) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseContainer executeRequestAsync(RequestContainer request, Templater templater, AsyncControl asyncControl) {
        return new TestRunner(requestExecutor).executeRequest((TestContainer) request, templater, asyncControl);
    }

    @Override
    public String getRequestType() {
        return "TEST";
    }

    @Override
    public boolean canHandle(RequestContainer request) {
        return request instanceof TestContainer;
    }

    @Override
    public void setRequestExecutor(PluginRequestExecutor executor) {
        requestExecutor = executor;
    }
}

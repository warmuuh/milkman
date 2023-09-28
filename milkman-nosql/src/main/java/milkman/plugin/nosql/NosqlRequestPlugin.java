package milkman.plugin.nosql;

import java.util.Collections;
import java.util.List;
import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;
import milkman.plugin.nosql.domain.NosqlParameterAspect;
import milkman.plugin.nosql.domain.NosqlQueryAspect;
import milkman.plugin.nosql.domain.NosqlRequestContainer;
import milkman.plugin.nosql.editor.NosqlQueryAspectEditor;
import milkman.plugin.nosql.editor.NosqlParametersAspectEditor;
import milkman.plugin.nosql.editor.NosqlRequestEditor;
import milkman.plugin.nosql.editor.NosqlResponseAspectEditor;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.RequestAspectsPlugin;
import milkman.ui.plugin.RequestTypeEditor;
import milkman.ui.plugin.RequestTypePlugin;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.ui.plugin.Templater;

public class NosqlRequestPlugin implements RequestTypePlugin, RequestAspectsPlugin {

  private NosqlRequestProcessor processor = new NosqlRequestProcessor();

  @Override
  public List<RequestAspectEditor> getRequestTabs() {
    return List.of(new NosqlParametersAspectEditor(), new NosqlQueryAspectEditor());
  }

  @Override
  public List<ResponseAspectEditor> getResponseTabs() {
    return List.of(new NosqlResponseAspectEditor());
  }

  @Override
  public void initializeRequestAspects(RequestContainer request) {
    if (request instanceof NosqlRequestContainer) {
      if (!request.getAspect(NosqlParameterAspect.class).isPresent()) {
        request.addAspect(new NosqlParameterAspect());
      }
      if (!request.getAspect(NosqlQueryAspect.class).isPresent()) {
        request.addAspect(new NosqlQueryAspect());
      }
    }
  }

  @Override
  public void initializeResponseAspects(RequestContainer request, ResponseContainer response, RequestExecutionContext context) {
    // we did it on our own, so nothing to do
  }

  @Override
  public RequestContainer createNewRequest() {
    return new NosqlRequestContainer("New NoSql Request", "");
  }

  @Override
  public RequestTypeEditor getRequestEditor() {
    return new NosqlRequestEditor();
  }

  @Override
  public ResponseContainer executeRequest(RequestContainer request, Templater templater) {
    return processor.executeRequest(request, templater);
  }

  @Override
  public String getRequestType() {
    return "NoSql";
  }

  @Override
  public boolean canHandle(RequestContainer request) {
    return request instanceof NosqlRequestContainer;
  }

  @Override
  public int getOrder() {
    return 22;
  }
}

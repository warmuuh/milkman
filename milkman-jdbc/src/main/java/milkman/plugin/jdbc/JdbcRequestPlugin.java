package milkman.plugin.jdbc;

import java.util.Collections;
import java.util.List;

import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;
import milkman.plugin.jdbc.domain.JdbcRequestContainer;
import milkman.plugin.jdbc.domain.JdbcSqlAspect;
import milkman.plugin.jdbc.editor.JdbcRequestEditor;
import milkman.plugin.jdbc.editor.JdbcResultSetAspectEditor;
import milkman.plugin.jdbc.editor.SqlAspectEditor;
import milkman.ui.plugin.CustomCommand;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.RequestAspectsPlugin;
import milkman.ui.plugin.RequestTypeEditor;
import milkman.ui.plugin.RequestTypePlugin;
import milkman.ui.plugin.ResponseAspectEditor;
import milkman.ui.plugin.Templater;

public class JdbcRequestPlugin implements RequestTypePlugin, RequestAspectsPlugin {


	JdbcQueryProcessor processor = new JdbcQueryProcessor();
	JdbcMetaProcessor metaProcessor = new JdbcMetaProcessor();
	
	@Override
	public RequestContainer createNewRequest() {
		return new JdbcRequestContainer("New Sql Request", "");
	}

	@Override
	public RequestTypeEditor getRequestEditor() {
		return new JdbcRequestEditor();
	}

	@Override
	public ResponseContainer executeRequest(RequestContainer request, Templater templater) {
		return processor.executeRequest(request, templater);
	}

	@Override
	public List<RequestAspectEditor> getRequestTabs() {
		return Collections.singletonList(new SqlAspectEditor());
	}

	@Override
	public List<ResponseAspectEditor> getResponseTabs() {
		return Collections.singletonList(new JdbcResultSetAspectEditor());
	}

	@Override
	public void initializeRequestAspects(RequestContainer request) {
		if (request instanceof JdbcRequestContainer) {
			if (!request.getAspect(JdbcSqlAspect.class).isPresent())
				request.addAspect(new JdbcSqlAspect());
		}
	}

	@Override
	public void initializeResponseAspects(RequestContainer request, ResponseContainer response, RequestExecutionContext context) {
		// we did it on our own, so nothing to do
	}

	@Override
	public String getRequestType() {
		return "SQL";
	}

	@Override
	public boolean canHandle(RequestContainer request) {
		return request instanceof JdbcRequestContainer;
	}

	@Override
	public int getOrder() {
		return 17;
	}

	@Override
	public List<CustomCommand> getCustomCommands() {
		return Collections.singletonList(new CustomCommand("SHOW_TABLES", "Show Tables"));
	}

	@Override
	public ResponseContainer executeCustomCommand(String commandId, RequestContainer request, Templater templater) {
		if (commandId.equals("SHOW_TABLES")) {
			return metaProcessor.showAllTables(request, templater);
		}
		throw new IllegalArgumentException("Custom command " + commandId + " not supported.");
	}
	
	


}

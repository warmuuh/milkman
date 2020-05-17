package milkman.plugin.jdbc;

import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;
import milkman.plugin.jdbc.domain.JdbcRequestContainer;
import milkman.plugin.jdbc.domain.JdbcSqlAspect;
import milkman.plugin.jdbc.editor.JdbcRequestEditor;
import milkman.plugin.jdbc.editor.JdbcResultSetAspectEditor;
import milkman.plugin.jdbc.editor.SqlAspectEditor;
import milkman.ui.plugin.*;

import java.util.Collections;
import java.util.List;

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
		return List.of(
				new CustomCommand("SHOW_TABLES", "Show Tables"),
				new CustomCommand("SHOW_COLUMNS", "Show Table Schema")
				);
	}

	@Override
	@SneakyThrows
	public ResponseContainer executeCustomCommand(String commandId, RequestContainer request, Templater templater) {
		if (commandId.equals("SHOW_TABLES")) {
			return metaProcessor.showAllTables(request, templater);
		} else if (commandId.equals("SHOW_COLUMNS")) {
			return metaProcessor.showTableInformation(request, templater);
		}
		throw new IllegalArgumentException("Custom command " + commandId + " not supported.");
	}
	
	


}

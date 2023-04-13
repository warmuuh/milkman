package milkman.plugin.cassandra;

import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;
import milkman.plugin.cassandra.domain.CassandraRequestContainer;
import milkman.plugin.cassandra.editor.CassandraRequestEditor;
import milkman.plugin.jdbc.domain.JdbcSqlAspect;
import milkman.ui.plugin.*;

import java.util.Collections;
import java.util.List;

public class CassandraRequestPlugin implements RequestTypePlugin, RequestAspectsPlugin {

	CassandraQueryProcessor processor = new CassandraQueryProcessor();

	@Override
	public RequestContainer createNewRequest() {
		return new CassandraRequestContainer("New Cql Request", "");
	}

	@Override
	public RequestTypeEditor getRequestEditor() {
		return new CassandraRequestEditor();
	}

	@Override
	public ResponseContainer executeRequest(RequestContainer request, Templater templater) {
		return processor.executeRequest(request, templater);
	}

	@Override
	public List<RequestAspectEditor> getRequestTabs() {
		return Collections.emptyList(); // we reuse sqlEditor from jdbc plugin
	}

	@Override
	public List<ResponseAspectEditor> getResponseTabs() {
		return Collections.emptyList(); // we reuse JdbcResultSetAspectEditor from jdbc plugin
	}

	@Override
	public void initializeRequestAspects(RequestContainer request) {
		if (request instanceof CassandraRequestContainer) {
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
		return "CQL";
	}

	@Override
	public boolean canHandle(RequestContainer request) {
		return request instanceof CassandraRequestContainer;
	}

	@Override
	public int getOrder() {
		return 20;
	}

}

package milkman.plugin.jdbc;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.domain.ResponseContainer.StyledText;
import milkman.plugin.jdbc.domain.JdbcRequestContainer;
import milkman.plugin.jdbc.domain.JdbcSqlAspect;
import milkman.plugin.jdbc.domain.RowSetResponseAspect;
import milkman.plugin.jdbc.domain.TableResponseContainer;
import milkman.ui.plugin.Templater;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Map;

@Slf4j
public class JdbcQueryProcessor extends AbstractJdbcProcessor {

	@SneakyThrows
	public ResponseContainer executeRequest(RequestContainer request, Templater templater) {
		
		if (!(request instanceof JdbcRequestContainer)) {
			throw new IllegalArgumentException("Unsupported request container: " + request.getType());
		}
		
		JdbcSqlAspect jdbcSqlAspect = request.getAspect(JdbcSqlAspect.class)
				.orElseThrow(() -> new IllegalArgumentException("Missing Sql Aspect"));
		String finalSql = templater.replaceTags(jdbcSqlAspect.getSql());
		
		
		JdbcRequestContainer jdbcRequest = (JdbcRequestContainer)request;
		String jdbcUrl = getJdbcUrl(jdbcRequest, templater);

		Connection connection = DriverManager.getConnection(jdbcUrl);
		Statement statement = connection.createStatement();
		long startTime = System.currentTimeMillis();
		boolean isResultSet = statement.execute(finalSql);
		long requestTimeInMs = System.currentTimeMillis() - startTime;
		
		TableResponseContainer response = new TableResponseContainer();
		RowSetResponseAspect rowSetAspect = new RowSetResponseAspect();
		
		if (isResultSet) {
			extractRows(statement.getResultSet(), rowSetAspect);
			response.getStatusInformations().complete(Map.of("Selected Rows", new StyledText(""+ rowSetAspect.getRows().size())));
		} else {
			response.getStatusInformations().complete(Map.of("Affected Rows", new StyledText(""+ statement.getUpdateCount())));
		}
		response.getStatusInformations().complete(Map.of("Time", new StyledText(requestTimeInMs + "ms")));

		response.getAspects().add(rowSetAspect);
		
		return response;
	}



}

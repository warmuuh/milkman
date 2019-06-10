package milkman.plugin.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;

import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.plugin.jdbc.domain.JdbcRequestContainer;
import milkman.plugin.jdbc.domain.JdbcResponseContainer;
import milkman.plugin.jdbc.domain.RowSetResponseAspect;
import milkman.ui.plugin.Templater;

public class JdbcMetaProcessor extends AbstractJdbcProcessor {
	@SneakyThrows
	public ResponseContainer showAllTables(RequestContainer request, Templater templater) {
		if (!(request instanceof JdbcRequestContainer)) {
			throw new IllegalArgumentException("Unsupported request container: " + request.getType());
		}
		JdbcRequestContainer jdbcRequest = (JdbcRequestContainer) request;
		String jdbcUrl = jdbcRequest.getJdbcUrl();
		
		Connection connection = DriverManager.getConnection(templater.replaceTags(jdbcUrl));
	
		DatabaseMetaData md = connection.getMetaData();
		ResultSet rs = md.getTables(null, null, "%", null);
		
		JdbcResponseContainer response = new JdbcResponseContainer();
		RowSetResponseAspect rowSetAspect = new RowSetResponseAspect();
		extractRows(rs, rowSetAspect);
		response.getAspects().add(rowSetAspect);
		response.getStatusInformations().put("Selected Rows", ""+ rowSetAspect.getRows().size());

		
		return response;
	}

}

package milkman.plugin.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.plugin.jdbc.domain.JdbcRequestContainer;
import milkman.plugin.jdbc.domain.JdbcResponseContainer;
import milkman.plugin.jdbc.domain.JdbcSqlAspect;
import milkman.plugin.jdbc.domain.RowSetResponseAspect;
import milkman.ui.plugin.Templater;

@Slf4j
public class JdbcQueryProcessor {

	@SneakyThrows
	public ResponseContainer executeRequest(RequestContainer request, Templater templater) {
		
		if (!(request instanceof JdbcRequestContainer)) {
			throw new IllegalArgumentException("Unsupported request container: " + request.getType());
		}
		
		JdbcSqlAspect sqlAspect = request.getAspect(JdbcSqlAspect.class)
				.orElseThrow(() -> new IllegalArgumentException("Missing Sql Aspect"));
		String finalSql = templater.replaceTags(sqlAspect.getSql());
		
		
		JdbcRequestContainer jdbcRequest = (JdbcRequestContainer)request;
		String jdbcUrl = jdbcRequest.getJdbcUrl();
		log.info("Executing Sql: " + finalSql);

		
		Connection connection = DriverManager.getConnection(templater.replaceTags(jdbcUrl));
		Statement statement = connection.createStatement();
		boolean isResultSet = statement.execute(finalSql);
		
		JdbcResponseContainer response = new JdbcResponseContainer();
		RowSetResponseAspect rowSetAspect = new RowSetResponseAspect();
		
		if (isResultSet) {
			extractRows(statement, rowSetAspect);
			response.getStatusInformations().put("Selected Rows", ""+ rowSetAspect.getRows().size());
		} else {
			response.getStatusInformations().put("Affected Rows", ""+statement.getUpdateCount());	
		}
		
		response.getAspects().add(rowSetAspect);
		
		return response;
	}

	private void extractRows(Statement statement, RowSetResponseAspect rowSetAspect) throws SQLException {
		ResultSet resultSet = statement.getResultSet();
		ResultSetMetaData metaData = resultSet.getMetaData();		
		
		List<String> columnNames = new LinkedList<String>();
		for(int i = 1; i <= metaData.getColumnCount(); ++i) { // column idx starts at 1
			columnNames.add(metaData.getColumnName(i));
		}
		rowSetAspect.setColumnNames(columnNames);
		
		while(resultSet.next()) {
			List<Object> row = new LinkedList<Object>();
			for(int i = 1; i <= metaData.getColumnCount(); ++i) {// column idx starts at 1
				row.add(resultSet.getObject(i));
			}
			rowSetAspect.addRow(row);
		}
	}

}

package milkman.plugin.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import milkman.plugin.jdbc.domain.RowSetResponseAspect;

public class AbstractJdbcProcessor {

	protected void extractRows(ResultSet resultSet, RowSetResponseAspect rowSetAspect) throws SQLException {
		ResultSetMetaData metaData = resultSet.getMetaData();		
		
		List<String> columnNames = new LinkedList<String>();
		for(int i = 1; i <= metaData.getColumnCount(); ++i) { // column idx starts at 1
			columnNames.add(metaData.getColumnName(i));
		}
		rowSetAspect.setColumnNames(columnNames);
		
		while(resultSet.next() && !maxRowLimitReached(rowSetAspect)) {
			List<Object> row = new LinkedList<Object>();
			for(int i = 1; i <= metaData.getColumnCount(); ++i) {// column idx starts at 1
				row.add(resultSet.getObject(i));
			}
			rowSetAspect.addRow(row);
		}
	}

	private boolean maxRowLimitReached(RowSetResponseAspect rowSetAspect) {
		return rowSetAspect.getRows().size() > JdbcOptionsProvider.options().getMaxRowFetchLimit();
	}
}

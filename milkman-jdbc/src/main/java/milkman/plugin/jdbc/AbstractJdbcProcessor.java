package milkman.plugin.jdbc;

import milkman.plugin.jdbc.domain.JdbcRequestContainer;
import milkman.plugin.jdbc.domain.RowSetResponseAspect;
import milkman.ui.main.options.CoreApplicationOptionsProvider;
import milkman.ui.plugin.Templater;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class AbstractJdbcProcessor {


	protected String getJdbcUrl(JdbcRequestContainer jdbcRequest, Templater templater) {
		String jdbcUrl = jdbcRequest.getJdbcUrl();
		String finalUrl = templater.replaceTags(jdbcUrl);

		if (CoreApplicationOptionsProvider.options().isUseSocksProxy()) {
			return jdbcUrlWithSocksProxy(finalUrl);
		}

		return finalUrl;
	}

	/**
	 * some drivers ignore environment variable and only use the parameters (jdbc for example)
	 * TODO: maybe we want to have a "customizer" by database-type
	 */
	private String jdbcUrlWithSocksProxy(String finalUrl) {
		String[] proxyAddress = CoreApplicationOptionsProvider.options().getSocksProxyAddress().split(":");
		String[] urlComponents = finalUrl.split("\\?");
		if (urlComponents.length == 1) {
			return finalUrl + "?socksProxyPort=" + proxyAddress[1] + "&socksProxyHost=" + proxyAddress[0] + "&socksProxyRemoteDns=true";
		} else {
			return finalUrl + "&socksProxyPort=" + proxyAddress[1] + "&socksProxyHost=" + proxyAddress[0] + "&socksProxyRemoteDns=true";
		}
	}

	protected void extractRows(ResultSet resultSet, RowSetResponseAspect rowSetAspect) throws SQLException {
		ResultSetMetaData metaData = resultSet.getMetaData();		
		
		List<String> columnNames = new LinkedList<String>();
		for(int i = 1; i <= metaData.getColumnCount(); ++i) { // column idx starts at 1
			columnNames.add(metaData.getColumnName(i));
		}
		rowSetAspect.setColumnNames(columnNames);
		
		while(resultSet.next() && !maxRowLimitReached(rowSetAspect)) {
			List<String> row = new LinkedList<>();
			for(int i = 1; i <= metaData.getColumnCount(); ++i) {// column idx starts at 1
				var value = resultSet.getObject(i);
				row.add(valueToString(value));
			}
			rowSetAspect.addRow(row);
		}
	}

	private String valueToString(Object value) {
		if (value instanceof Blob) {
			try {
				value = IOUtils.toString(((Blob) value).getBinaryStream());
			} catch (IOException | SQLException e) {
				value = "BLOB";
			}
		}
		String stringValue = value != null ? value.toString() : "NULL";
		return stringValue;
	}

	private boolean maxRowLimitReached(RowSetResponseAspect rowSetAspect) {
		return rowSetAspect.getRows().size() > JdbcOptionsProvider.options().getMaxRowFetchLimit();
	}
}

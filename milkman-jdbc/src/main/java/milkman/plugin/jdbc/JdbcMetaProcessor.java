package milkman.plugin.jdbc;

import javafx.application.Platform;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.domain.ResponseContainer.StyledText;
import milkman.plugin.jdbc.domain.JdbcRequestContainer;
import milkman.plugin.jdbc.domain.RowSetResponseAspect;
import milkman.plugin.jdbc.domain.TableResponseContainer;
import milkman.ui.main.dialogs.StringInputDialog;
import milkman.ui.plugin.Templater;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class JdbcMetaProcessor extends AbstractJdbcProcessor {
	@SneakyThrows
	public ResponseContainer showAllTables(RequestContainer request, Templater templater) {
		if (!(request instanceof JdbcRequestContainer)) {
			throw new IllegalArgumentException("Unsupported request container: " + request.getType());
		}
		JdbcRequestContainer jdbcRequest = (JdbcRequestContainer) request;
		String jdbcUrl = getJdbcUrl(jdbcRequest, templater);

		Connection connection = DriverManager.getConnection(jdbcUrl);
	
		DatabaseMetaData md = connection.getMetaData();
		ResultSet rs = md.getTables(null, null, "%", null);
		
		TableResponseContainer response = new TableResponseContainer();
		RowSetResponseAspect rowSetAspect = new RowSetResponseAspect();
		extractRows(rs, rowSetAspect);
		response.getAspects().add(rowSetAspect);
		response.getStatusInformations().complete(Map.of("Selected Rows", new StyledText(""+ rowSetAspect.getRows().size())));

		
		return response;
	}
	
	@SneakyThrows
	public ResponseContainer showTableInformation(RequestContainer request, Templater templater) {
		if (!(request instanceof JdbcRequestContainer)) {
			throw new IllegalArgumentException("Unsupported request container: " + request.getType());
		}
		JdbcRequestContainer jdbcRequest = (JdbcRequestContainer) request;
		String jdbcUrl = jdbcRequest.getJdbcUrl();
		
		var tableName = getTableName();

		Connection connection = DriverManager.getConnection(templater.replaceTags(jdbcUrl));
	
		DatabaseMetaData md = connection.getMetaData();
		ResultSet rs = md.getColumns(null, null, tableName, "%");
		
		TableResponseContainer response = new TableResponseContainer();
		RowSetResponseAspect rowSetAspect = new RowSetResponseAspect();
		extractRows(rs, rowSetAspect);
		response.getAspects().add(rowSetAspect);
		response.getStatusInformations().complete(Map.of("Selected Rows", new StyledText(""+ rowSetAspect.getRows().size())));

		
		return response;
	}

	private String getTableName() throws InterruptedException {
		StringInputDialog dialog = new StringInputDialog();
		CountDownLatch latch = new CountDownLatch(1);
		Platform.runLater(() -> {
			dialog.showAndWait("Show Table Schema", "Show schema for table", "");
			latch.countDown();
		});
		latch.await();
		if (!dialog.isCancelled()) {
			String tableName = dialog.getInput();
			return tableName.toUpperCase();
		} else {
			throw new RuntimeException("Command aborted");
		}
		
	}
	
	
	
	
	

}

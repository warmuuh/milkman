package milkman.plugin.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.shaded.guava.common.collect.Streams;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.plugin.cassandra.domain.CassandraRequestContainer;
import milkman.plugin.jdbc.domain.JdbcSqlAspect;
import milkman.plugin.jdbc.domain.RowSetResponseAspect;
import milkman.plugin.jdbc.domain.TableResponseContainer;
import milkman.ui.plugin.Templater;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class CassandraQueryProcessor {

	@SneakyThrows
	public ResponseContainer executeRequest(RequestContainer request, Templater templater) {
		
		if (!(request instanceof CassandraRequestContainer)) {
			throw new IllegalArgumentException("Unsupported request container: " + request.getType());
		}
		
		JdbcSqlAspect jdbcSqlAspect = request.getAspect(JdbcSqlAspect.class)
				.orElseThrow(() -> new IllegalArgumentException("Missing Sql Aspect"));
		String finalCql = templater.replaceTags(jdbcSqlAspect.getSql());


		CassandraRequestContainer jdbcRequest = (CassandraRequestContainer)request;
		String cassandraUrl = templater.replaceTags(jdbcRequest.getCassandraUrl());
		log.info("Executing Cql: " + finalCql);


		var url = new URI(cassandraUrl);
		var conProps = CassandraConnectionProperties.fromUri(url);
		return executeCql(finalCql, conProps);
	}

	private TableResponseContainer executeCql(String finalCql, CassandraConnectionProperties conProps) {
		TableResponseContainer response = new TableResponseContainer();


		RowSetResponseAspect rowSetAspect = new RowSetResponseAspect();
		var builder = CqlSession.builder()
				.withLocalDatacenter(conProps.getDatacenter())
				.addContactPoint(InetSocketAddress.createUnresolved(conProps.getHost(), conProps.getPort()));

		if (StringUtils.isNotBlank(conProps.getKeyspace())) {
			builder = builder.withKeyspace("\"" + conProps.getKeyspace() + "\"");
		}

		if (StringUtils.isNotBlank(conProps.getUser())) {
			builder = builder.withAuthCredentials(conProps.getUser(), conProps.getPassword() == null ? "" : conProps.getPassword());
		}

		long requestTimeInMs = 0;
		try (CqlSession session = builder.build()) {
			long startTime = System.currentTimeMillis();
			ResultSet rs = session.execute(finalCql);
			requestTimeInMs = System.currentTimeMillis() - startTime;

			var columnNames = Streams.stream(rs.getColumnDefinitions())
					.map(cd -> cd.getName().asCql(true))
					.collect(Collectors.toList());

			var rows = new LinkedList<List<String>>();
			for (Row row : rs) {
				List<String> rowValues = new LinkedList<>();
				for(int c = 0; c < columnNames.size(); ++c){
					var value = row.getObject(c);
					rowValues.add(value != null ? value.toString() : "NULL");
				}
				rows.add(rowValues);
			}

			rowSetAspect.setColumnNames(columnNames);
			rowSetAspect.setRows(rows);
		}

		response.getAspects().add(rowSetAspect);
		response.getStatusInformations().complete(Map.of(
				"Rows", ""+ rowSetAspect.getRows().size(),
				"Time", requestTimeInMs + "ms"));


		return response;
	}




}

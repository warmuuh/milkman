package milkman.plugin.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.config.OptionsMap;
import com.datastax.oss.driver.api.core.config.TypedDriverOption;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.shaded.guava.common.collect.Streams;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.domain.ResponseContainer.StyledText;
import milkman.plugin.cassandra.domain.CassandraRequestContainer;
import milkman.plugin.cassandra.proxy.SocksProxyAwareSessionBuilder;
import milkman.plugin.jdbc.domain.JdbcSqlAspect;
import milkman.plugin.jdbc.domain.RowSetResponseAspect;
import milkman.plugin.jdbc.domain.TableResponseContainer;
import milkman.ui.plugin.Templater;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class CassandraQueryProcessor {


	private static final Map<CassandraConnectionProperties, CqlSession> connectionCache = new HashMap<>();

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> connectionCache.forEach((prop, con) -> {
			try {
				con.closeAsync().toCompletableFuture().get(2, TimeUnit.SECONDS);
			} catch (Exception e) {
				log.warn("Failed to properly close cassandra connection");
			}
		})));
	}

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

		long requestTimeInMs = 0;
		CqlSession session = openCqlSession(conProps);
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


		response.getAspects().add(rowSetAspect);
		response.getStatusInformations().complete(Map.of(
				"Rows", new StyledText(""+ rowSetAspect.getRows().size()),
				"Time", new StyledText(requestTimeInMs + "ms")));


		return response;
	}

	private CqlSession openCqlSession(CassandraConnectionProperties conProps) {
		if (connectionCache.containsKey(conProps)){
			CqlSession cachedSession = connectionCache.get(conProps);
			if (!cachedSession.isClosed()) {
				return cachedSession;
			} else {
				connectionCache.remove(conProps);
			}
		}

		var builder = new SocksProxyAwareSessionBuilder()
				.withLocalDatacenter(conProps.getDatacenter())
				.addContactPoint(InetSocketAddress.createUnresolved(conProps.getHost(), conProps.getPort()));

		if (StringUtils.isNotBlank(conProps.getKeyspace())) {
			builder = builder.withKeyspace("\"" + conProps.getKeyspace() + "\"");
		}

		if (StringUtils.isNotBlank(conProps.getUser())) {
			builder = builder.withAuthCredentials(conProps.getUser(), conProps.getPassword() == null ? "" : conProps.getPassword());
		}

		var map = OptionsMap.driverDefaults();
		map.put(TypedDriverOption.NETTY_DAEMON, true);
		builder.withConfigLoader(DriverConfigLoader.fromMap(map));

		var cqlSession = builder.build();
		connectionCache.put(conProps, cqlSession);

		return cqlSession;
	}


}

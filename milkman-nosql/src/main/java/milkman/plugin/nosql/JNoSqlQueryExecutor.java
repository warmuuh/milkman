package milkman.plugin.nosql;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.eclipse.jnosql.communication.Entry;
import org.eclipse.jnosql.communication.Settings;
import org.eclipse.jnosql.communication.column.Column;
import org.eclipse.jnosql.communication.column.ColumnConfiguration;
import org.eclipse.jnosql.communication.document.Document;
import org.eclipse.jnosql.communication.document.DocumentConfiguration;
import org.eclipse.jnosql.communication.keyvalue.KeyValueConfiguration;

@FunctionalInterface
public interface JNoSqlQueryExecutor {

  @SneakyThrows
  static JNoSqlQueryExecutor getQueryExecutorFromProperties(Map<String, Object> properties) {
    if (properties.containsKey("jnosql.document.provider")) {
      return createDocumentProvider(properties);
    }

    if (properties.containsKey("jnosql.column.provider")) {
      return createColumnProvider(properties);
    }

    if (properties.containsKey("jnosql.keyvalue.provider")) {
      return createKeyValueProvider(properties);
    }

    throw new IllegalArgumentException("only document, keyvalue or column providers are supported for now. no valid jnosql provider found");
  }

  Stream<Map<String, Object>> query(String database, String query);

  private static JNoSqlQueryExecutor createDocumentProvider(Map<String, Object> properties) throws ClassNotFoundException {
    String providerClass = properties.get("jnosql.document.provider").toString();

    DocumentConfiguration configuration = DocumentConfiguration.getConfiguration((Class<DocumentConfiguration>) Class.forName(providerClass));

    return (database, query) -> {
      try (var factory = configuration.apply(Settings.of(properties))) {
        var manager = factory.apply(database);
        return manager.query(query).map(document -> document.documents().stream().collect(Collectors.toMap(
            Entry::name,
            Document::get
        )));
      }
    };
  }

  private static JNoSqlQueryExecutor createColumnProvider(Map<String, Object> properties) throws ClassNotFoundException {
    String providerClass = properties.get("jnosql.column.provider").toString();

    ColumnConfiguration configuration = ColumnConfiguration.getConfiguration((Class<ColumnConfiguration>) Class.forName(providerClass));

    return (database, query) -> {
      try (var factory = configuration.apply(Settings.of(properties))) {
        var manager = factory.apply(database);
        return manager.query(query).map(column -> column.columns().stream().collect(Collectors.toMap(
            Entry::name,
            Column::get
        )));
      }
    };
  }

  private static JNoSqlQueryExecutor createKeyValueProvider(Map<String, Object> properties) throws ClassNotFoundException {
    String providerClass = properties.get("jnosql.keyvalue.provider").toString();

    KeyValueConfiguration configuration = KeyValueConfiguration.getConfiguration((Class<KeyValueConfiguration>) Class.forName(providerClass));

    return (database, query) -> {
      try (var factory = configuration.apply(Settings.of(properties))) {
        var manager = factory.apply(database);
        return manager.query(query).map(value -> Map.of("result", value.get()));
      }
    };
  }
}

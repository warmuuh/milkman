package milkman.plugin.nosql;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.domain.ResponseContainer.StyledText;
import milkman.plugin.nosql.domain.NosqlParameterAspect;
import milkman.plugin.nosql.domain.NosqlQueryAspect;
import milkman.plugin.nosql.domain.NosqlRequestContainer;
import milkman.plugin.nosql.domain.NosqlResponseAspect;
import milkman.plugin.nosql.domain.NosqlResponseContainer;
import milkman.ui.plugin.Templater;

public class NosqlRequestProcessor {

  @SneakyThrows
  public ResponseContainer executeRequest(RequestContainer request, Templater templater) {
    if (!(request instanceof NosqlRequestContainer)) {
      throw new IllegalArgumentException("No Nosql request");
    }

    String database = templater.replaceTags(((NosqlRequestContainer) request).getDatabase());

    NosqlQueryAspect query = request.getAspect(NosqlQueryAspect.class)
        .orElseThrow(() -> new IllegalArgumentException("Missing Nosql Query Aspect"));

    Map<String, Object> params = request.getAspect(NosqlParameterAspect.class)
        .orElseThrow(() -> new IllegalArgumentException("Missing Nosql Parameter Aspect"))
        .getEntries().stream().collect(Collectors.toMap(
            e -> templater.replaceTags(e.getName()),
            e -> templater.replaceTags(e.getValue())
        ));

    JNoSqlQueryExecutor queryExecutor = JNoSqlQueryExecutor.getQueryExecutorFromProperties(params);
    long requestTimeInMs;

    long startTime = System.currentTimeMillis();
    Stream<Map<String, Object>> resultStream = queryExecutor.query(database, query.getQuery());
    requestTimeInMs = System.currentTimeMillis() - startTime;

    Set<String> keys = new LinkedHashSet<>();

    List<List<String>> resultRows = resultStream.map(document -> {
      keys.addAll(document.keySet());
      return keys.stream()
          .map(k -> Optional.ofNullable(document.get(k)).map(Object::toString).orElse(null))
          .toList();
    }).collect(Collectors.toList());

    NosqlResponseAspect responseAspect = new NosqlResponseAspect();
    responseAspect.setColumnNames(new LinkedList<>(keys));
    responseAspect.setRows(resultRows);

    NosqlResponseContainer response = new NosqlResponseContainer();
    response.getStatusInformations().complete(Map.of(
        "Rows", new StyledText(String.valueOf(resultRows.size())),
        "Time", new StyledText(requestTimeInMs + "ms")
    ));

    response.getAspects().add(responseAspect);
    return response;
  }
}

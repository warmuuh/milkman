package milkman.plugin.nosql;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.plugin.nosql.domain.NosqlParameterAspect;
import milkman.plugin.nosql.domain.NosqlQueryAspect;
import milkman.plugin.nosql.domain.NosqlRequestContainer;
import milkman.plugin.nosql.domain.NosqlResponseAspect;
import milkman.plugin.nosql.domain.NosqlResponseContainer;
import milkman.ui.plugin.Templater;
import org.eclipse.jnosql.communication.Settings;
import org.eclipse.jnosql.communication.document.DocumentConfiguration;
import org.eclipse.jnosql.communication.document.DocumentEntity;

public class NosqlRequestProcessor {

  @SneakyThrows
  public ResponseContainer executeRequest(RequestContainer request, Templater templater) {
    if (!(request instanceof NosqlRequestContainer)) {
      throw new IllegalArgumentException("No Nosql request");
    }

    String database = templater.replaceTags(((NosqlRequestContainer) request).getUrl());

    NosqlQueryAspect query = request.getAspect(NosqlQueryAspect.class)
        .orElseThrow(() -> new IllegalArgumentException("Missing Nosql Query Aspect"));

    Map<String, Object> params = request.getAspect(NosqlParameterAspect.class)
        .orElseThrow(() -> new IllegalArgumentException("Missing Nosql Parameter Aspect"))
        .getEntries().stream().collect(Collectors.toMap(
            e -> templater.replaceTags(e.getName()),
            e -> templater.replaceTags(e.getValue())
        ));

    if (!params.containsKey("jnosql.document.provider")) {
      throw new IllegalArgumentException("only document providers are supported for now or jnosql.document.provider parameter missing");
    }

    String providerClass = params.get("jnosql.document.provider").toString();

    DocumentConfiguration configuration = DocumentConfiguration.getConfiguration((Class<DocumentConfiguration>) Class.forName(providerClass));
    try (var factory = configuration.apply(Settings.of(params))) {
      var manager = factory.apply(database);
      Stream<DocumentEntity> resultStream = manager.query(query.getQuery());

      Set<String> keys = new LinkedHashSet<>();

      List<List<String>> resultRows = resultStream.map(document -> {
        keys.addAll(document.getDocumentNames());
        return keys.stream()
            .map(k -> document.find(k).map(entry -> entry.get().toString()).orElse(null))
            .toList();
      }).collect(Collectors.toList());

      NosqlResponseAspect responseAspect = new NosqlResponseAspect();
      responseAspect.setColumnNames(new LinkedList<>(keys));
      responseAspect.setRows(resultRows);

      NosqlResponseContainer response = new NosqlResponseContainer();
      response.getAspects().add(responseAspect);
      return response;
    }
  }
}

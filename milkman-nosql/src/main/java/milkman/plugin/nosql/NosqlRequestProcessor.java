package milkman.plugin.nosql;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import milkman.plugin.nosql.domain.ParameterEntry;
import milkman.ui.plugin.Templater;
import org.eclipse.jnosql.communication.Settings;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.keyvalue.BucketManager;
import org.eclipse.jnosql.communication.keyvalue.BucketManagerFactory;
import org.eclipse.jnosql.communication.keyvalue.KeyValueConfiguration;

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

    if (!params.containsKey("jnosql.keyvalue.provider")) {
      throw new IllegalArgumentException("only keyvalue providers are supported for now or jnosql.keyvalue.provider parameter missing");
    }

    String keyvalueProviderClass = params.get("jnosql.keyvalue.provider").toString();

    KeyValueConfiguration configuration = KeyValueConfiguration.getConfiguration((Class<KeyValueConfiguration>) Class.forName(keyvalueProviderClass));
    try (BucketManagerFactory factory = configuration.apply(Settings.of(params))) {
      BucketManager bucketManager = factory.apply(database);

      Stream<Value> resultStream = bucketManager.query(query.getQuery());
      List<List<String>> resultRows = resultStream.map(v -> List.of(v.get().toString())).collect(Collectors.toList());
      NosqlResponseAspect responseAspect = new NosqlResponseAspect();
      responseAspect.setColumnNames(List.of("results"));
      responseAspect.setRows(resultRows);

      NosqlResponseContainer response = new NosqlResponseContainer();
      response.getAspects().add(responseAspect);
      return response;
    }
  }
}

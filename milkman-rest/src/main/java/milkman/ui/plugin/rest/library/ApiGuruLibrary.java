package milkman.ui.plugin.rest.library;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.Value;
import milkman.domain.Collection;
import milkman.ui.plugin.LibraryPlugin;
import milkman.ui.plugin.rest.openapi.OpenapiImporterV30;
import milkman.ui.plugin.rest.openapi.OpenapiImporterV30.NewEnvironemtKey;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

public class ApiGuruLibrary implements LibraryPlugin {

  Map<String, List<ApiGuruEntry>> libraryCache = new ConcurrentHashMap<>();

  @Override
  public String getName() {
    return "Apis.guru";
  }

  @Override
  public String getDefaultUrl() {
    return "https://api.apis.guru/v2/list.json";
  }

  @Override
  public List<LibraryEntry> lookupEntry(String url, String searchInput) {
    return getLibrary(url).stream()
        .filter(e -> e.getDisplayName().toLowerCase().contains(searchInput))
        .map(e -> new LibraryEntry(e.getDisplayName(), e.getId()))
        .collect(Collectors.toList());
  }

  private List<ApiGuruEntry> getLibrary(String url) {
    List<ApiGuruEntry> cachedEntries = libraryCache.get(url);
    if (cachedEntries != null) {
      return cachedEntries;
    }

    List<ApiGuruEntry> entries = loadLibrary(url);
    libraryCache.put(url, entries);
    return entries;
  }

  @SneakyThrows
  private List<ApiGuruEntry> loadLibrary(String url) {
    LinkedList<ApiGuruEntry> result = new LinkedList<>();
    JsonNode root = new ObjectMapper().readTree(new URL(url));
    Iterator<Entry<String, JsonNode>> fields = root.fields();
    while (fields.hasNext()) {
      Entry<String, JsonNode> field = fields.next();
      String apiKey = field.getKey();
      Iterator<Entry<String, JsonNode>> versions = field.getValue().get("versions").fields();
      while(versions.hasNext()) {
        Entry<String, JsonNode> versionEntry = versions.next();
        String apiVersion = versionEntry.getKey();
        String apiTitle = versionEntry.getValue().get("info").get("title").asText();
        String apiYamlUrl = versionEntry.getValue().get("swaggerYamlUrl").asText();
        result.add(new ApiGuruEntry(
            apiTitle + " (" + apiVersion + ")",
            apiKey + ":" + apiVersion,
            apiYamlUrl
            ));
      }
    }
    return result;
  }

  @Override
  public Collection importCollection(String url, String libraryEntryId) {
    return getLibrary(url).stream()
        .filter(entry -> entry.getId().equals(libraryEntryId))
        .findAny()
        .map(this::loadSwaggerCollection)
        .orElseThrow(() -> new IllegalArgumentException("entry " + libraryEntryId + " not found in library " + url));
  }

  @SneakyThrows
  private Collection loadSwaggerCollection(ApiGuruEntry entry) {
    String swaggerYaml = IOUtils.toString(URI.create(entry.getYamlUrl()), Charset.defaultCharset());
    Pair<Collection, List<NewEnvironemtKey>> result = new OpenapiImporterV30().importCollection(
        swaggerYaml, Collections.emptyList());
    return result.getKey();
  }

  @Value
  private static class ApiGuruEntry {
    String displayName;
    String id;
    String yamlUrl;
  }
}

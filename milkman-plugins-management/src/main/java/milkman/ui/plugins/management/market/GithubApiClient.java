package milkman.ui.plugins.management.market;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import lombok.SneakyThrows;

public class GithubApiClient {


  private ObjectMapper mapper = new ObjectMapper();

  public GithubApiClient() {
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }

  @SneakyThrows
  public List<MarketplacePlugin> fetchPluginRepos() {
    return searchForRepos("milkman-plugins")
        .items().stream()
        .flatMap(res -> loadPluginDescriptorForRepo(res.fullName())
            .stream())
        .toList();
  }

  private GithubRepoSearchResultList searchForRepos(String topic) throws IOException {
    InputStream inputStream = new URL(
        "https://api.github.com/search/repositories?q=topic:" + topic +
            "&sort=created&order=asc").openStream();
    GithubRepoSearchResultList searchResult =
        mapper.readValue(inputStream, GithubRepoSearchResultList.class);
    return searchResult;
  }

  @SneakyThrows
  private List<MarketplacePlugin> loadPluginDescriptorForRepo(String fullRepoName) {
    HttpURLConnection con =
        (HttpURLConnection) new URL("https://api.github.com/repos/" + fullRepoName +
            "/contents/milkman-plugin.json").openConnection();
    con.setRequestProperty("Accept", "application/vnd.github.raw+json");
    con.connect();

    if (con.getResponseCode() != 200) {
      return List.of();
    }

    GithubPluginRepoDescription result =
        mapper.readValue(con.getInputStream(), GithubPluginRepoDescription.class);


    return result.plugins().stream().map(
        p -> new MarketplacePlugin(p.author(), p.name(), p.description(), p.artifact(),
            "https://github.com/" + fullRepoName + "/releases/latest/download/" + p.artifact(),
            "https:/github.com/" + fullRepoName)
    ).toList();
  }


  record GithubRepoSearchResultList(
      List<GithubRepoSearchResult> items
  ) {

  }

  record GithubRepoSearchResult(@JsonProperty("full_name") String fullName) {

  }

  public record GithubPluginRepoDescription(List<GithubPluginDescription> plugins) {
  }

  public record GithubPluginDescription(String author, String name, String artifact, String description) {

  }

  public record MarketplacePlugin(String author, String name, String description,String artifactFilename, String artifactUrl, String documentationUrl) {
  }

}

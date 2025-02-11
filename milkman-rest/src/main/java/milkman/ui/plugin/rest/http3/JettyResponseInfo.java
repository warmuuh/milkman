package milkman.ui.plugin.rest.http3;

import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse.ResponseInfo;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.Value;
import org.eclipse.jetty.http.MetaData;

@Value
class JettyResponseInfo implements ResponseInfo {

  MetaData.Response responseInfo;

  @Override
  public int statusCode() {
    return responseInfo.getStatus();
  }

  @Override
  public HttpHeaders headers() {
    Map<String, List<String>> headers = new HashMap<>();
    responseInfo.getHttpFields().forEach(header ->
        headers.computeIfAbsent(header.getName(), name -> new LinkedList<>())
            .add(header.getValue()));

    return HttpHeaders.of(headers, (a, b) -> true);
  }

  @Override
  public Version version() {
    return null;
  }
}

package milkman.plugin.ws.insomnia;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import milkman.domain.RequestContainer;
import milkman.plugin.ws.domain.WebsocketAspect;
import milkman.plugin.ws.domain.WebsocketRequestContainer;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.insomnia.InsomniaImporterV4.InsomniaFile;
import milkman.ui.plugin.rest.insomnia.InsomniaImporterV4.InsomniaResource;
import milkman.ui.plugin.rest.insomnia.InsomniaImporterV4.InsomniaWsRequest;
import milkman.ui.plugin.rest.insomnia.InsomniaRequestImporter;

public class InsomniaWsRequestImporter implements InsomniaRequestImporter {

  @Override
  public boolean supportRequestType(InsomniaResource resource) {
    return resource instanceof InsomniaWsRequest;
  }

  @Override
  public RequestContainer convert(InsomniaResource resource, List<InsomniaFile> files) {
    InsomniaWsRequest wsRequest = (InsomniaWsRequest) resource;

    String url = wsRequest.getUrl();
    if (!wsRequest.getParameters().isEmpty()) {
      url += "?" + wsRequest.getParameters().stream().map(p -> p.getName() + "="+p.getValue()).collect(Collectors.joining("&"));
    }

    WebsocketRequestContainer request = new WebsocketRequestContainer(wsRequest.getName(), url);
    request.setId(UUID.randomUUID().toString());
    request.setInStorage(true);

    //adding headers
    RestHeaderAspect headers = new RestHeaderAspect();
    wsRequest.getHeaders().forEach(h -> headers.getEntries().add(new HeaderEntry(UUID.randomUUID().toString(),h.getName(), h.getValue(), !h.isDisabled())));
    //only basic auth supported for now
    if (wsRequest.getAuthentication() != null && wsRequest.getAuthentication().getType() != null && wsRequest.getAuthentication().getType().equals("basic")) {
      headers.getEntries().add(new HeaderEntry(UUID.randomUUID().toString(), "Authorization", "{{js:base64('"
          +wsRequest.getAuthentication().getUsername() + ":" + wsRequest.getAuthentication().getPassword() + "')}}",
          !wsRequest.getAuthentication().isDisabled()));
    }
    request.addAspect(headers);

    WebsocketAspect body = new WebsocketAspect();
    request.addAspect(body);

    return request;
  }
}

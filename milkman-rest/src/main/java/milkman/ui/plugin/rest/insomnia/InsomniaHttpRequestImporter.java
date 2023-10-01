package milkman.ui.plugin.rest.insomnia;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import milkman.domain.RequestContainer;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestBodyAspect;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import milkman.ui.plugin.rest.insomnia.InsomniaImporterV4.InsomniaFile;
import milkman.ui.plugin.rest.insomnia.InsomniaImporterV4.InsomniaHttpRequest;
import milkman.ui.plugin.rest.insomnia.InsomniaImporterV4.InsomniaRequest;
import milkman.ui.plugin.rest.insomnia.InsomniaImporterV4.InsomniaResource;

public class InsomniaHttpRequestImporter implements InsomniaRequestImporter{

  @Override
  public boolean supportRequestType(InsomniaResource resource) {
    return resource instanceof InsomniaHttpRequest;
  }

  @Override
  public RequestContainer convert(InsomniaResource resource, List<InsomniaFile> files) {
    InsomniaHttpRequest iReq = (InsomniaHttpRequest) resource;

    //it is a request

    String url = iReq.getUrl();
    if (!iReq.getParameters().isEmpty()) {
      url += "?" + iReq.getParameters().stream().map(p -> p.getName() + "="+p.getValue()).collect(Collectors.joining("&"));
    }

    RestRequestContainer request = new RestRequestContainer(iReq.getName(), url, iReq.getMethod());

    request.setId(UUID.randomUUID().toString());
    request.setInStorage(true);

    //adding headers
    RestHeaderAspect headers = new RestHeaderAspect();
    iReq.getHeaders().forEach(h -> headers.getEntries().add(new HeaderEntry(UUID.randomUUID().toString(),h.getName(), h.getValue(), !h.isDisabled())));
    //only basic auth supported for now
    if (iReq.getAuthentication() != null && iReq.getAuthentication().getType() != null && iReq.getAuthentication().getType().equals("basic")) {
      headers.getEntries().add(new HeaderEntry(UUID.randomUUID().toString(), "Authorization", "{{js:base64('"
          +iReq.getAuthentication().getUsername() + ":" + iReq.getAuthentication().getPassword() + "')}}",
          !iReq.getAuthentication().isDisabled()));
    }
    request.addAspect(headers);

    //adding bodies
    RestBodyAspect body = new RestBodyAspect();
    if (iReq.getBody() != null && iReq.getBody().getText() != null) {
      body.setBody(iReq.getBody().getText());
      if (iReq.getBody().getMimeType() != null) {
        headers.getEntries().add(new HeaderEntry(UUID.randomUUID().toString(), "Content-Type", iReq.getBody().getMimeType(), true));
      }
    }
    request.addAspect(body);
    return request;
  }
}

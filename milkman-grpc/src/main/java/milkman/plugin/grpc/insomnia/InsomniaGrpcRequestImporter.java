package milkman.plugin.grpc.insomnia;

import java.util.List;
import java.util.UUID;
import milkman.domain.RequestContainer;
import milkman.plugin.grpc.domain.GrpcHeaderAspect;
import milkman.plugin.grpc.domain.GrpcOperationAspect;
import milkman.plugin.grpc.domain.GrpcPayloadAspect;
import milkman.plugin.grpc.domain.GrpcRequestContainer;
import milkman.ui.plugin.rest.insomnia.InsomniaImporterV4.InsomniaFile;
import milkman.ui.plugin.rest.insomnia.InsomniaImporterV4.InsomniaGrpcRequest;
import milkman.ui.plugin.rest.insomnia.InsomniaImporterV4.InsomniaResource;
import milkman.ui.plugin.rest.insomnia.InsomniaRequestImporter;

public class InsomniaGrpcRequestImporter implements InsomniaRequestImporter {

  @Override
  public boolean supportRequestType(InsomniaResource resource) {
    return resource instanceof InsomniaGrpcRequest;
  }

  @Override
  public RequestContainer convert(InsomniaResource resource, List<InsomniaFile> files) {

    InsomniaGrpcRequest grpcReq = (InsomniaGrpcRequest) resource;

    GrpcRequestContainer request = new GrpcRequestContainer(grpcReq.getName(), grpcReq.getUrl());

    request.setId(UUID.randomUUID().toString());
    request.setInStorage(true);

    //adding headers
    GrpcHeaderAspect headers = new GrpcHeaderAspect();
//    grpcReq.getHeaders().forEach(h -> headers.getEntries().add(new HeaderEntry(UUID.randomUUID().toString(),h.getName(), h.getValue(), !h.isDisabled())));
    //only basic auth supported for now
    request.addAspect(headers);

    //adding bodies
    GrpcPayloadAspect body = new GrpcPayloadAspect();
    if (grpcReq.getBody() != null && grpcReq.getBody().getText() != null) {
      body.setPayload(grpcReq.getBody().getText());
    }
    request.addAspect(body);

    //adding operation
    GrpcOperationAspect operation = new GrpcOperationAspect();
    operation.setOperation(grpcReq.getMethod());
    files.stream().filter(f -> f.getId().equals(grpcReq.getProtoFileId()))
            .findAny().ifPresent(file -> operation.setProtoSchema(file.getBody()));
    request.addAspect(operation);
    return request;
  }
}

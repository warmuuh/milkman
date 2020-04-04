package milkman.ui.plugin.rest.openapi;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.milkman.rest.openapi.schema.v3.Openapi30;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import milkman.domain.Collection;
import milkman.domain.Folder;
import milkman.domain.RequestContainer;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestBodyAspect;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OpenapiImporterV30 {
    public Collection importCollection(String content) throws IOException {
        SwaggerParseResult res = new OpenAPIV3Parser().readContents(content);
        OpenAPI spec = res.getOpenAPI();


        LinkedList<RequestContainer> requests = new LinkedList<>();
        LinkedList<Folder> folders = new LinkedList<>();

        String host = spec.getServers().stream().findAny().map(s -> s.getUrl()).orElse("http://localhost:8080");

        spec.getPaths().entrySet().stream()
            .map(p -> toRequests(host, p.getKey(), p.getValue()))
            .forEach(requests::addAll);



        Collection collection = new Collection(UUID.randomUUID().toString(), spec.getInfo().getTitle(), false, requests, folders);

        return collection;
    }

    private  List<RequestContainer> toRequests(String host, String path, PathItem pathItem) {
        return pathItem.readOperationsMap().entrySet().stream()
                .map(op -> toRequests(host, op.getKey(), path, op.getValue()))
                .collect(Collectors.toList());
    }

    private RequestContainer toRequests(String host, PathItem.HttpMethod method, String path, Operation operation) {

        String qryStr = "";
        if (operation.getParameters() != null){
            qryStr = operation.getParameters().stream()
                    .filter(p -> p.getIn().equals("query") && p.getRequired())
                    .map(p -> p.getName() + "=")
                    .collect(Collectors.joining("&", "?", ""));
            //if it only contains prefix
            if (qryStr.length() == 1){
                qryStr = "";
            }
        }


        RestRequestContainer container = new RestRequestContainer(operation.getOperationId(), host + path + qryStr, method.name());



        RestHeaderAspect headers = new RestHeaderAspect();
        container.addAspect(headers);

        if (operation.getParameters() != null) {
            operation.getParameters().stream()
                    .filter(p -> p.getIn().equals("header") && p.getRequired())
                    .map(p -> new HeaderEntry(UUID.randomUUID().toString(), p.getName(), "", true))
                    .forEach(headers.getEntries()::add);
        }

        RestBodyAspect body = new RestBodyAspect();
        container.addAspect(body);

        return container;
    }


}

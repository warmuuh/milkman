package milkman.ui.plugin.rest.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.util.Objects;
import lombok.Value;
import milkman.domain.Collection;
import milkman.domain.Environment;
import milkman.domain.Folder;
import milkman.domain.RequestContainer;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestBodyAspect;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class OpenapiImporterV30 {


    public Pair<Collection, List<NewEnvironemtKey>> importCollection(String content, List<Environment> environments) throws IOException {
        SwaggerParseResult res = new OpenAPIV3Parser().readContents(content);
        OpenAPI spec = res.getOpenAPI();


        LinkedList<RequestContainer> requests = new LinkedList<>();
        LinkedList<Folder> folders = new LinkedList<>();
        LinkedList<NewEnvironemtKey> newEnvironemtKeys = new LinkedList<>();

        int idx = 0;
        for (Server server : spec.getServers()) {
            String serverId = toServerId(server, spec, idx);
            newEnvironemtKeys.add(new NewEnvironemtKey("", serverId, server.getUrl()));
            idx++;
        }

        String host = newEnvironemtKeys.stream().findAny().map(k -> "{{"+k.getKeyName()+"}}").orElse("http://localhost:8080");

        spec.getPaths().entrySet().stream()
            .map(p -> toRequests(host, p.getKey(), p.getValue()))
            .forEach(requests::addAll);

        Collection collection = new Collection(UUID.randomUUID().toString(), spec.getInfo().getTitle(), false, requests, folders);

        return Pair.of(collection, newEnvironemtKeys);
    }

    private String toServerId(Server server, OpenAPI spec, int idx) {
        var serverPrefix = toId(spec.getInfo().getTitle());
        var serverId = toId(server.getDescription());
        if (serverId.isEmpty()){
            serverId = "server"+idx;
        }
        return serverPrefix.isEmpty() ? serverId : serverPrefix + "." + serverId;
    }

    private String toId(String string) {
        if (string == null) {
            return "";
        }
        return string
                .toLowerCase()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-zA-Z0-9\\-]", "");
    }

    private  List<RequestContainer> toRequests(String host, String path, PathItem pathItem) {
        return pathItem.readOperationsMap().entrySet().stream()
                .map(op -> toRequests(host, op.getKey(), path, op.getValue()))
                .collect(Collectors.toList());
    }

    private RequestContainer toRequests(String host, HttpMethod method, String path, Operation operation) {

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


        String requestName = ObjectUtils.firstNonNull(operation.getOperationId(), operation.getSummary(), path);
        
        RestRequestContainer container = new RestRequestContainer(requestName, host + path + qryStr, method.name());



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
        container.setInStorage(true);
        return container;
    }


    @Value
    public static class NewEnvironemtKey {
        String environmentId;
        String keyName;
        String keyValue;
    }
}

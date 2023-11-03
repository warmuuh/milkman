package milkman.plugin.auth.oauth.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import milkman.ui.main.dialogs.StringInputDialog;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;

@Slf4j
public class AuthorizationCodeCaptureServer {


    private final Mono<String> code;

    private final HttpServer server;

    public AuthorizationCodeCaptureServer() throws IOException {
        StringInputDialog portQueryDialog = new StringInputDialog();
        portQueryDialog.showAndWait("Authentication server port",
            "Choose port to listen to when returning from authentication flow:", "8080");
        if (portQueryDialog.isCancelled()) {
            throw new IllegalStateException("Autnentication Server cancelled");
        }
        int port = Integer.parseInt(portQueryDialog.getInput());
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        code = Mono.create(this::startServer)
                .doFinally(s -> stopServer());
    }

    public Mono<String> listenForCode() {
        return code;
    }

    @SneakyThrows
    private void startServer(MonoSink<String> sink) {

        server.createContext("/", ex -> {
            Headers rHeaders = ex.getResponseHeaders();
            rHeaders.set("Content-Type", "text/plain");
            ex.sendResponseHeaders(200, 0);

            if ("GET".equalsIgnoreCase(ex.getRequestMethod())){
                var params = splitQuery(ex.getRequestURI());
                if (params.containsKey("code")){
                    String responseMessage = "Authorization grant recieved. you can close this window";
                    writeResponse(ex, responseMessage);
                    sink.success(params.get("code"));
                } else {
                    String error = params.get("error");
                    String errorMessage = error != null ? error : "No Errormessage provided";
                    String responseMessage = "No Authorization code received. Error: " + errorMessage;
                    writeResponse(ex, responseMessage);
                    sink.error(new IllegalStateException("No Code Received: " + errorMessage));
                }
            }
        });

        server.start();

        var port = server.getAddress().getPort();
        log.info("Server started at http://localhost:" + port);
    }

    private void stopServer() {
        log.info("Server stopped");
        server.stop(0);
    }

    private void writeResponse(HttpExchange ex, String responseMessage) {
        try (OutputStream responseStream = ex.getResponseBody()) {
            responseStream.write(responseMessage.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getReturnUrl(){
        return "http://localhost:" + server.getAddress().getPort() + "/";
    }

//    public static void main(String[] args) throws Exception {
//        var server = new AuthorizationCodeCaptureServer();
//        System.out.println("Redirect url: " + server.getReturnUrl());
//        var code = server.listenForCode();
//        System.out.println("Received auth code:" + code.block());
//    }

    @SneakyThrows
    public static Map<String, String> splitQuery(URI url)  {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        String query = url.getQuery();
        if (StringUtils.isBlank(query)){
            return Collections.emptyMap();
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8), URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8));
        }
        return query_pairs;
    }

}

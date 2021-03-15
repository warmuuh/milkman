package milkman.plugin.auth.oauth.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

@Slf4j
public class AuthorizationCodeCaptureServer {


    private final Mono<String> code;

    private final HttpServer server;

    public AuthorizationCodeCaptureServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
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
                var params = parseQueryParams(ex.getRequestURI().toString());
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

    public Map<String, String> parseQueryParams(String url) {
        Map<String, String> entries = new HashMap<>();
        int indexOf = url.indexOf('?');
        if (indexOf > 0) {
            String paramString = url.substring(indexOf+1);
            String[] paramPairs = paramString.split("&");
            for(String pair : paramPairs) {
                String[] splittedPair = pair.split("=");
                String key = splittedPair.length > 0 ? splittedPair[0] : "";
                String value = splittedPair.length > 1 ? splittedPair[1] : "";
                entries.put(key, value);
            }
        }
        return entries;
    }

}

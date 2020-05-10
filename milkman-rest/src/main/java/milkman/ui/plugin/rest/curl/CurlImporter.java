package milkman.ui.plugin.rest.curl;

import javafx.scene.Node;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.Workspace;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.ImporterPlugin;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestBodyAspect;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import milkman.ui.plugin.rest.postman.ImportControl;
import org.apache.commons.io.IOUtils;

import java.util.*;

@Slf4j
public class CurlImporter implements ImporterPlugin {

    private ImportControl importCtrl;

    @Override
    public String getName() {
        return "Curl";
    }

    @Override
    public Node getImportControls() {
        importCtrl = new ImportControl(false);
        return importCtrl;
    }

    @Override @SneakyThrows
    public boolean importInto(Workspace workspace, Toaster toaster) {
        try{
            var command = IOUtils.toString(importCtrl.getInput());
            var args = translateCommandline(command);

            if (args.length < 2 || !args[0].equals("curl")){
                toaster.showToast("No valid curl command");
                return false;
            }
            var argumentList = Arrays.asList(args);

            String url = extractUrl(argumentList);
            String method = extractMethod(argumentList);

            var request = new RestRequestContainer("New Request", url, method);
            var headerAspect = new RestHeaderAspect();
            headerAspect.setEntries(extractHeaders(argumentList));
            request.addAspect(headerAspect);

            var bodyAspect = new RestBodyAspect();
            bodyAspect.setBody(extractBody(argumentList));
            request.addAspect(bodyAspect);

            workspace.getOpenRequests().add(request);
            workspace.setActiveRequest(request);
            return true;

        } catch (RuntimeException e){
            log.error("Failed to parse curl command", e);
            toaster.showToast("No valid curl command");
            return false;
        }
    }

    private String extractBody(List<String> argumentList) {
        var values = getArgumentValues(argumentList, List.of("-d", "--data"));
        return values.stream().findFirst().orElse("");
    }

    private String extractMethod(List<String> argumentList) {
        var values = getArgumentValues(argumentList, List.of("-X", "--request"));
        return values.stream().findFirst().orElse("GET");
    }

    private String extractUrl(List<String> argumentList) {
        var values = getArgumentValues(argumentList, List.of("--url"));
        if (values.size() > 0){
            return values.get(0);
        }

        for (int i = 1; i < argumentList.size(); i++) {
            String argument = argumentList.get(i);

            if (argument.startsWith("-")){
                i += 1;
                continue;
            }
            return argument;
        }

        throw new RuntimeException("Failed to extract url");
    }


    List<HeaderEntry> extractHeaders(List<String> arguments) {
        List<String> values = getArgumentValues(arguments, List.of("-H", "--header"));

        List<HeaderEntry> headers = new LinkedList<>();
        for (String header : values) {
            int firstColon = header.indexOf(':');
            if (firstColon >= 0){
                String headerName = header.substring(0, firstColon).trim();
                String headerValue = header.substring(firstColon+1).trim();
                headers.add(new HeaderEntry(UUID.randomUUID().toString(), headerName, headerValue, true));
            }
        }

        return headers;
    }

    private List<String> getArgumentValues(List<String> arguments, List<String> argumentNames) {
        List<String> values = new LinkedList<>();
        for (int i = 0; i < arguments.size(); i++) {
            String argument = arguments.get(i);
            if (argumentNames.contains(argument) && arguments.size() > i +1){
                values.add(arguments.get(i + 1));
            }
        }
        return values;
    }


    /**
     * Crack a command line.
     *
     * copied from https://github.com/apache/jmeter/blob/master/src/protocol/http/src/main/java/org/apache/jmeter/protocol/http/curl/BasicCurlParser.java
     *
     * @param toProcess the command line to process.
     * @return the command line broken into strings.
     * An empty or null toProcess parameter results in a zero sized array.
     */
    public static String[] translateCommandline(String toProcess) {
        if (toProcess == null || toProcess.isEmpty()) {
            //no command? no string
            return new String[0];
        }

        //remove continuations
        String input = toProcess.replaceAll("(?im)[\\^|\\\\]\\R", "");

        // parse with a simple finite state machine
        final int normal = 0;
        final int inQuote = 1;
        final int inDoubleQuote = 2;
        int state = normal;
        final StringTokenizer tok = new StringTokenizer(input, "\"\' ", true);
        final ArrayList<String> result = new ArrayList<>();
        final StringBuilder current = new StringBuilder();
        boolean lastTokenHasBeenQuoted = false;

        while (tok.hasMoreTokens()) {
            String nextTok = tok.nextToken();
            switch (state) {
                case inQuote:
                    if ("\'".equals(nextTok)) {
                        lastTokenHasBeenQuoted = true;
                        state = normal;
                    } else {
                        current.append(nextTok);
                    }
                    break;
                case inDoubleQuote:
                    if ("\"".equals(nextTok)) {
                        lastTokenHasBeenQuoted = true;
                        state = normal;
                    } else {
                        current.append(nextTok);
                    }
                    break;
                default:
                    if ("\'".equals(nextTok)) {
                        state = inQuote;
                    } else if ("\"".equals(nextTok)) {
                        state = inDoubleQuote;
                    } else if (" ".equals(nextTok)) {
                        if (lastTokenHasBeenQuoted || current.length() > 0) {
                            result.add(current.toString());
                            current.setLength(0);
                        }
                    } else {
                        current.append(nextTok);
                    }
                    lastTokenHasBeenQuoted = false;
                    break;
            }
        }
        if (lastTokenHasBeenQuoted || current.length() > 0) {
            result.add(current.toString());
        }
        if (state == inQuote || state == inDoubleQuote) {
            throw new IllegalArgumentException("unbalanced quotes in " + toProcess);
        }
        return result.toArray(new String[result.size()]);
    }
}

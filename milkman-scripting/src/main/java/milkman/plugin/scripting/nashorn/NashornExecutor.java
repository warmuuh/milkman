package milkman.plugin.scripting.nashorn;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;
import milkman.plugin.scripting.ScriptExecutor;
import milkman.plugin.scripting.ScriptOptionsProvider;
import milkman.ui.main.Toaster;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.graalvm.polyglot.Source;

import javax.script.*;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class NashornExecutor implements ScriptExecutor {
    static ScriptEngine engine = initScriptEngine();
    private static Map<String, String> preloadScriptCache = new HashMap<>();

    private final Toaster toaster;

    private static ScriptEngine initScriptEngine() {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        return engine;
    }

    @Override
    public String executeScript(String source, RequestContainer request, ResponseContainer response, RequestExecutionContext context) {
        ByteArrayOutputStream logStream = new ByteArrayOutputStream();
        Bindings bindings = engine.createBindings();
        engine.setContext(new SimpleScriptContext());
        engine.getContext().setErrorWriter(new OutputStreamWriter(logStream));
        engine.getContext().setWriter(new OutputStreamWriter(logStream));
        var facade = new MilkmanNashornFacade(request, response, context, toaster);
        bindings.put("milkman", facade);
        bindings.put("mm", facade);

        try {
            //nashorn-polyfill.js
            engine.eval("var global = this;\n" +
                                "var window = this;\n" +
                                "var process = {env:{}};\n" +
                                "\n" +
                                "var console = {};\n" +
                                "console.debug = print;\n" +
                                "console.log = print;\n" +
                                "console.warn = print;\n" +
                                "console.error = print;", bindings);
            updatePreloadCache();
            for (String preloadScript : preloadScriptCache.values()) {
                engine.eval(preloadScript, bindings);
            }
            Object eval = engine.eval("with (global){" + source + "}", bindings);
        } catch (Exception e) {
            String causeMessage = ExceptionUtils.getRootCauseMessage(e);
            toaster.showToast("Failed to execute script: " + causeMessage);
            log.error("failed to execute script", e);
        }
        return logStream.toString();
    }

    private void updatePreloadCache() throws URISyntaxException, IOException {
        List<String> preloadScripts = ScriptOptionsProvider.options().getPreloadScripts();

        for (String preloadScriptUrl : preloadScripts) {
            if (!preloadScriptCache.containsKey(preloadScriptUrl)){
                URI uri = new URI(preloadScriptUrl);
                String path = uri.getPath();
                String filename = path.substring(path.lastIndexOf('/') + 1);

                String libSrc = IOUtils.toString(uri);
                preloadScriptCache.put(preloadScriptUrl, "with (global){" + libSrc + "}");
            }
        }
        preloadScriptCache.entrySet().removeIf(e -> !preloadScripts.contains(e.getKey()));
    }
}

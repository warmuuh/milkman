package milkman.plugin.scripting.graaljs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;
import milkman.plugin.scripting.ScriptExecutor;
import milkman.plugin.scripting.ScriptOptionsProvider;
import milkman.plugin.scripting.nashorn.MilkmanNashornFacade;
import milkman.ui.main.Toaster;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.graalvm.polyglot.*;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class GraaljsExecutor implements ScriptExecutor {
    private static Engine engine = initEngine();
    private static Map<String, Source> preloadScriptCache = new HashMap<>();

    private static Engine initEngine() {
        return Engine.newBuilder()
                .build();
    }

    private final Toaster toaster;

    private Context createContext(ByteArrayOutputStream logStream) {
      return Context.newBuilder("js")
                    .allowAllAccess(true)
                    .option("js.nashorn-compat", "true") //allow bean-properties to be accessed via fieldname
                    .engine(engine)
                    .out(logStream)
                    .err(logStream)
                    .build();
    }

    @Override
    public ExecutionResult executeScript(String source, RequestContainer request, ResponseContainer response, RequestExecutionContext context) {
        ByteArrayOutputStream logStream = new ByteArrayOutputStream();
        try(Context ctx = createContext(logStream)){
            var facade = new MilkmanGraalJsFacade(request, response, context, toaster);
            var bindings = ctx.getBindings("js");
            bindings.putMember("milkman", facade);
            bindings.putMember("mm", facade);


            try {
                updatePreloadCache();
                ctx.eval(Source.create("js", "var global = {};"));
                preloadScriptCache.values().forEach(ctx::eval);
                Value js = ctx.eval(Source.create("js", "with(global){" + source + "}"));
                return new ExecutionResult(logStream.toString(), Optional.ofNullable(js));
            } catch (PolyglotException e) {
                String causemessage = ExceptionUtils.getRootCauseMessage(e);
                //we cant access actual exception in js, so we need to do this to have a bit shorter exception
                var failedDueToIdx = causemessage.indexOf("failed due to:");
                if (failedDueToIdx > 0){
                    causemessage = causemessage.substring(failedDueToIdx);
                }
                toaster.showToast("Failed to execute script: " + causemessage);
                log.error("failed to execute script", e);
            } catch (Exception e) {
                String causeMessage = ExceptionUtils.getRootCauseMessage(e);
                toaster.showToast("Failed to execute script: " + causeMessage);
                log.error("failed to execute script", e);
            }
        }

        return new ExecutionResult(logStream.toString(), Optional.empty());
    }

    private void updatePreloadCache() throws URISyntaxException, IOException {
        List<String> preloadScripts = ScriptOptionsProvider.options().getPreloadScripts();

        for (String preloadScriptUrl : preloadScripts) {
            if (!preloadScriptCache.containsKey(preloadScriptUrl)){
                URI uri = new URI(preloadScriptUrl);
                String path = uri.getPath();
                String filename = path.substring(path.lastIndexOf('/') + 1);

                String libSrc = IOUtils.toString(uri);
                Source preloadLib = Source.newBuilder("js", " with(global){" + libSrc + "}", filename)
                        .cached(true)
                        .build();
                preloadScriptCache.put(preloadScriptUrl, preloadLib);
            }
        }
        preloadScriptCache.entrySet().removeIf(e -> !preloadScripts.contains(e.getKey()));
    }
}

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

import javax.script.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class NashornExecutor implements ScriptExecutor {
    static ScriptEngine engine = initScriptEngine();

    private static Integer preloadScriptCacheHash = 0;
    private static Bindings globalBindings;

    private final Toaster toaster;

    private static ScriptEngine initScriptEngine() {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        return engine;
    }

    @Override
    public ExecutionResult executeScript(String source, RequestContainer request, ResponseContainer response, RequestExecutionContext context) {
        ByteArrayOutputStream logStream = new ByteArrayOutputStream();
        initGlobalBindings();

        Bindings bindings = engine.createBindings();
        engine.setContext(new SimpleScriptContext());

        //we need to use globalBindings as engnine bindings and the normal bindings as globalBindings, otherwise things like chai dont work
        //bc they modify object prototype and this change is not resolved if in global scope

        engine.getContext().setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
        engine.getContext().setBindings(globalBindings, ScriptContext.ENGINE_SCOPE);

        engine.getContext().setErrorWriter(new OutputStreamWriter(logStream));
        engine.getContext().setWriter(new OutputStreamWriter(logStream));

        var facade = new MilkmanNashornFacade(request, response, context, toaster);
        bindings.put("milkman", facade);
        bindings.put("mm", facade);

        try {
            Object eval = engine.eval(source);
            return new ExecutionResult(logStream.toString(), Optional.ofNullable(eval), Optional.empty());
        } catch (Exception e) {
            log.error("failed to execute script", e);
            return new ExecutionResult(logStream.toString(), Optional.empty(), Optional.of(e));
        }
    }



    public void initGlobalBindings() {
        List<String> preloadScripts = ScriptOptionsProvider.options().getPreloadScripts();

        int currentHash = preloadScripts.hashCode();
        if (preloadScriptCacheHash == currentHash){
            return;
        }

        try {
            Bindings bindings = engine.createBindings();
            engine.setContext(new SimpleScriptContext());
            engine.eval(new InputStreamReader(getClass().getResourceAsStream("/nashorn-polyfill.js")), bindings);
            engine.eval(new InputStreamReader(getClass().getResourceAsStream("/jsHashes.js")), bindings);
            engine.eval(new InputStreamReader(getClass().getResourceAsStream("/custom-script-setup.js")), bindings);

            for (String preloadScriptUrl : preloadScripts) {
                URI uri = new URI(preloadScriptUrl);
                String path = uri.getPath();
                String filename = path.substring(path.lastIndexOf('/') + 1);

                String libSrc = IOUtils.toString(uri); //"with (global){" + IOUtils.toString(uri) + "}";
                engine.eval(libSrc, bindings);
            }

//            ((ScriptObjectMirror)bindings).freeze();
            this.globalBindings = bindings;
            this.preloadScriptCacheHash = currentHash;
            return;
        } catch (Exception e) {
            String causeMessage = ExceptionUtils.getRootCauseMessage(e);
            toaster.showToast("Failed to initialize preloader scripts: " + causeMessage);
            log.error("failed to execute script", e);
        }
    }


}

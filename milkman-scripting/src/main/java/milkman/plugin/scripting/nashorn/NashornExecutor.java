package milkman.plugin.scripting.nashorn;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;
import milkman.plugin.scripting.ScriptExecutor;
import milkman.ui.main.Toaster;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;

@Slf4j
@RequiredArgsConstructor
public class NashornExecutor implements ScriptExecutor {
    static ScriptEngine engine = initScriptEngine();

    private final Toaster toaster;

    private static ScriptEngine initScriptEngine() {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        return engine;
    }

    @Override
    public String executeScript(String source, RequestContainer request, ResponseContainer response, RequestExecutionContext context) {
        ByteArrayOutputStream logStream = new ByteArrayOutputStream();
        Bindings bindings = engine.createBindings();
        engine.getContext().setErrorWriter(new OutputStreamWriter(logStream));
        engine.getContext().setWriter(new OutputStreamWriter(logStream));
        var facade = new MilkmanNashornFacade(request, response, context, toaster);
        bindings.put("milkman", facade);
        bindings.put("mm", facade);

        try {
            Object eval = engine.eval(source, bindings);
        } catch (ScriptException e) {
            String causeMessage = ExceptionUtils.getRootCauseMessage(e);
            toaster.showToast("Failed to execute script: " + causeMessage);
            log.error("failed to execute script", e);
        }
        return logStream.toString();
    }
}

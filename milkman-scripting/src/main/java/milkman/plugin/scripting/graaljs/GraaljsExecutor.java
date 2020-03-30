package milkman.plugin.scripting.graaljs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.RequestContainer;
import milkman.domain.RequestExecutionContext;
import milkman.domain.ResponseContainer;
import milkman.plugin.scripting.ScriptExecutor;
import milkman.plugin.scripting.nashorn.MilkmanNashornFacade;
import milkman.ui.main.Toaster;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.graalvm.polyglot.*;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.ByteArrayOutputStream;

@Slf4j
@RequiredArgsConstructor
public class GraaljsExecutor implements ScriptExecutor {
    private static Engine engine = initEngine();

    private static Engine initEngine() {
        return Engine.newBuilder()
                .build();
    }

    private final Toaster toaster;

    private Context createContext(ByteArrayOutputStream logStream) {
      return Context.newBuilder("js")
                    .allowAllAccess(true)
                    .engine(engine)
                    .out(logStream)
                    .err(logStream)
                    .build();
    }

    @Override
    public String executeScript(String source, RequestContainer request, ResponseContainer response, RequestExecutionContext context) {
        ByteArrayOutputStream logStream = new ByteArrayOutputStream();
        try(Context ctx = createContext(logStream)){
            var facade = new MilkmanGraalJsFacade(request, response, context, toaster);
            var bindings = ctx.getBindings("js");
            bindings.putMember("milkman", facade);
            bindings.putMember("mm", facade);

            try {
                ctx.eval(Source.create("js", source));
            }catch (PolyglotException e) {
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

        return logStream.toString();
    }
}

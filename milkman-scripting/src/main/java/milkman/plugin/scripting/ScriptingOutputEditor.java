package milkman.plugin.scripting;

import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import lombok.val;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.ui.plugin.ResponseAspectEditor;
import org.apache.commons.lang3.StringUtils;

public class ScriptingOutputEditor implements ResponseAspectEditor {
    @Override
    public Tab getRoot(RequestContainer request, ResponseContainer response) {
        val output = response.getAspect(ScriptingOutputAspect.class).get();

        StringBuilder b = new StringBuilder();
        if (StringUtils.isNotEmpty(output.getPreScriptOutput())){
            b.append("-- pre script output -- \n");
            b.append(output.getPreScriptOutput());
            b.append("\n");
        }

        if (StringUtils.isNotEmpty(output.getPostScriptOutput())){
            b.append("-- post script output -- \n");
            b.append(output.getPostScriptOutput());
            b.append("\n");
        }

        var area = new TextArea(b.toString());
        area.setEditable(false);

        return new Tab("Script Output", area);
    }

    @Override
    public boolean canHandleAspect(RequestContainer request, ResponseContainer response) {
        return response.getAspect(ScriptingOutputAspect.class)
                .filter(a -> StringUtils.isNotEmpty(a.getPreScriptOutput()) || StringUtils.isNotEmpty(a.getPostScriptOutput()))
                .isPresent();
    }
}

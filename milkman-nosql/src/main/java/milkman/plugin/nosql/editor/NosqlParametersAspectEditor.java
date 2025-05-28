package milkman.plugin.nosql.editor;

import static milkman.utils.FunctionalUtils.run;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.scene.control.Tab;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.plugin.nosql.domain.NosqlParameterAspect;
import milkman.plugin.nosql.domain.ParameterEntry;
import milkman.ui.components.AutoCompleter;
import milkman.ui.components.TableEditor;
import milkman.ui.plugin.AutoCompletionAware;
import milkman.ui.plugin.RequestAspectEditor;
import org.apache.commons.io.IOUtils;

public class NosqlParametersAspectEditor implements RequestAspectEditor, AutoCompletionAware {


	private AutoCompleter completer;
	private static final Pattern propertyPattern = Pattern.compile("(?://)?(\\S+)=(.*)");
	private static List<String> parameters;
	
	@Override
	@SneakyThrows
	public Tab getRoot(RequestContainer request) {
		NosqlParameterAspect params = request.getAspect(NosqlParameterAspect.class).get();
		TableEditor<ParameterEntry> editor = new TableEditor<ParameterEntry>("nosql.parameters.list");
		editor.enableAddition(() -> {
			Platform.runLater( () -> params.setDirty(true));
			return new ParameterEntry(UUID.randomUUID().toString(), "", "", true);
		});
		editor.addCheckboxColumn("Enabled", 
				ParameterEntry::isEnabled, 
				run(ParameterEntry::setEnabled).andThen(() -> params.setDirty(true)));
		
		List<String> parameterList = getNosqlParameters();
		editor.addColumn("Name",
				ParameterEntry::getName, 
				run(ParameterEntry::setName).andThen(() -> params.setDirty(true)),
				tf -> completer.attachStaticCompletionTo(tf, parameterList));
		editor.addColumn("Value", 
				ParameterEntry::getValue,
				run(ParameterEntry::setValue).andThen(() -> params.setDirty(true)),
				tf -> completer.attachVariableCompletionTo(tf));
		editor.addDeleteColumn("Delete", (removed) -> params.setDirty(true));

		editor.setRowToStringConverter(this::headerToString);
		editor.setStringToRowConverter(this::stringToHeader);
		
		editor.setItems(params.getEntries());
		
		params.onInvalidate.clear();
		params.onInvalidate.add(() -> editor.setItems(params.getEntries()));
		
		
		return new Tab("Parameters", editor);
	}

	@Override
	public boolean canHandleAspect(RequestContainer request) {
		return request.getAspect(NosqlParameterAspect.class).isPresent();
	}
	
	private String headerToString(ParameterEntry header) {
		String prefix = "";
		if (!header.isEnabled())
			prefix = "//";
		return prefix + header.getName() + ": " + header.getValue();
	}

	public ParameterEntry stringToHeader(String headerStr) {
		boolean enabled = true;
		if (headerStr.startsWith("//"))
			enabled = false;

		Matcher matcher = propertyPattern.matcher(headerStr);
		if (matcher.matches()) {
			String key = matcher.group(1);
			String value = matcher.group(2);
			return new ParameterEntry(UUID.randomUUID().toString(), key.trim(), value.trim(), enabled);
		}

		return null;
	}
	
	@Override
	public void setAutoCompleter(AutoCompleter completer) {
		this.completer = completer;
	}
	
	@SneakyThrows
	private static List<String> getNosqlParameters() {
		if (parameters == null) {
			InputStream inputStream = NosqlParametersAspectEditor.class.getResourceAsStream("/parameters.txt");
			parameters = IOUtils.readLines(inputStream);
		}
		return parameters;
	}

}

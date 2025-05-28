package milkman.plugin.grpc.editor;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.plugin.grpc.domain.GrpcHeaderAspect;
import milkman.plugin.grpc.domain.HeaderEntry;
import milkman.ui.components.AutoCompleter;
import milkman.ui.components.TableEditor;
import milkman.ui.plugin.AutoCompletionAware;
import milkman.ui.plugin.RequestAspectEditor;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static milkman.utils.FunctionalUtils.run;

public class GrpcHeaderAspectEditor implements RequestAspectEditor, AutoCompletionAware {


	private static final Pattern headerPattern = Pattern.compile("(?://)?(\\S+):(.*)");
	private AutoCompleter completer;
	
	@Override
	@SneakyThrows
	public Tab getRoot(RequestContainer request) {
		GrpcHeaderAspect headers = request.getAspect(GrpcHeaderAspect.class).get();
		TableEditor<HeaderEntry> editor = new TableEditor<HeaderEntry>("grpc.headers.list");
		editor.enableAddition(() -> {
			Platform.runLater( () -> headers.setDirty(true));
			return new HeaderEntry(UUID.randomUUID().toString(), "", "", true);
		});
		editor.addCheckboxColumn("Enabled", 
				HeaderEntry::isEnabled, 
				run(HeaderEntry::setEnabled).andThen(() -> headers.setDirty(true)));
		
		editor.addColumn("Name",
				HeaderEntry::getName, 
				run(HeaderEntry::setName).andThen(() -> headers.setDirty(true)));
		editor.addColumn("Value", 
				HeaderEntry::getValue,
				run(HeaderEntry::setValue).andThen(() -> headers.setDirty(true)),
				tf -> completer.attachVariableCompletionTo(tf));
		editor.addDeleteColumn("Delete", (removed) -> headers.setDirty(true));

		editor.setRowToStringConverter(this::headerToString);
		editor.setStringToRowConverter(this::stringToHeader);
		
		editor.setItems(headers.getEntries());
		
		headers.onInvalidate.clear();
		headers.onInvalidate.add(() -> editor.setItems(headers.getEntries()));
		
		return new Tab("Headers", editor);
	}

	@Override
	public boolean canHandleAspect(RequestContainer request) {
		return request.getAspect(GrpcHeaderAspect.class).isPresent();
	}

	@Override
	public void setAutoCompleter(AutoCompleter completer) {
		this.completer = completer;
	}


	private String headerToString(HeaderEntry header) {
		String prefix = "";
		if (!header.isEnabled())
			prefix = "//";
		return prefix + header.getName() + ": " + header.getValue();
	}

	public HeaderEntry stringToHeader(String headerStr) {
		boolean enabled = true;
		if (headerStr.startsWith("//"))
			enabled = false;

		Matcher matcher = headerPattern.matcher(headerStr);
		if (matcher.matches()) {
			String key = matcher.group(1);
			String value = matcher.group(2);
			return new HeaderEntry(UUID.randomUUID().toString(), key.trim(), value.trim(), enabled);
		}

		return null;
	}
	


}

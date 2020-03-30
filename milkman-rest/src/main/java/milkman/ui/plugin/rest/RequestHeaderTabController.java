package milkman.ui.plugin.rest;

import static milkman.utils.FunctionalUtils.run;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ClassPathUtils;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import lombok.SneakyThrows;
import milkman.domain.RequestContainer;
import milkman.ui.components.AutoCompleter;
import milkman.ui.components.JfxTableEditor;
import milkman.ui.plugin.AutoCompletionAware;
import milkman.ui.plugin.RequestAspectEditor;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.utils.fxml.FxmlUtil;

public class RequestHeaderTabController implements RequestAspectEditor, AutoCompletionAware {


	private AutoCompleter completer;
	private static Pattern headerPattern = Pattern.compile("(?://)?(\\S+):(.*)");
	private static List<String> headers;
	
	@Override
	@SneakyThrows
	public Tab getRoot(RequestContainer request) {
		RestHeaderAspect headers = request.getAspect(RestHeaderAspect.class).get();
		JfxTableEditor<HeaderEntry> editor = new JfxTableEditor<HeaderEntry>();
		editor.enableAddition(() -> {
			Platform.runLater( () -> headers.setDirty(true));
			return new HeaderEntry(UUID.randomUUID().toString(), "", "", true);
		});
		editor.addCheckboxColumn("Enabled", 
				HeaderEntry::isEnabled, 
				run(HeaderEntry::setEnabled).andThen(() -> headers.setDirty(true)));
		
		List<String> headerList = getHeaders();
		editor.addColumn("Name",
				HeaderEntry::getName, 
				run(HeaderEntry::setName).andThen(() -> headers.setDirty(true)),
				tf -> completer.attachStaticCompletionTo(tf, headerList));
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
		return request.getAspect(RestHeaderAspect.class).isPresent();
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
	
	@Override
	public void setAutoCompleter(AutoCompleter completer) {
		this.completer = completer;
	}
	
	@SneakyThrows
	private static List<String> getHeaders() {
		if (headers == null) {
			InputStream inputStream = RequestHeaderTabController.class.getResourceAsStream("/headers.txt");
			headers = IOUtils.readLines(inputStream);
		}
		return headers;
	}

}

package milkman.ui.components;

import javafx.scene.control.TextField;
import javafx.util.Callback;
import lombok.RequiredArgsConstructor;
import milkman.domain.Environment.EnvironmentEntry;
import milkman.ui.plugin.TemplateParameterResolverPlugin;
import milkman.utils.controlfx.PartialAutoCompletion;
import org.controlsfx.control.textfield.AutoCompletionBinding.ISuggestionRequest;
import org.controlsfx.control.textfield.TextFields;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RequiredArgsConstructor(onConstructor_={@Inject})
public class AutoCompleter {

	private final Supplier<List<EnvironmentEntry>> activeEnvironmentEntrySupplier;
	private final Supplier<List<TemplateParameterResolverPlugin>> templaters;


	public void attachVariableCompletionTo(TextField textField) {
		new PartialAutoCompletion<>(textField, new Callback<ISuggestionRequest, Collection<String>>() {
			@Override
			public Collection<String> call(ISuggestionRequest req) {
				String userText = req.getUserText();

				var prefixedKeys = templaters.get().stream()
						.flatMap(t -> t.getAllEntries().stream().map(k -> t.getPrefix() + ":" + k));
				var envKeys = activeEnvironmentEntrySupplier.get().stream()
						.map(EnvironmentEntry::getName);

				return Stream.concat(envKeys, prefixedKeys)
					.filter(k -> k.contains(userText))
					.filter(k -> !k.isBlank())
					.collect(Collectors.toList());
			}
		});
	}
	
	public void attachStaticCompletionTo(TextField textField, Collection<String> possibleSuggestions) {
		TextFields.bindAutoCompletion(textField, possibleSuggestions);
	}
	
	public void attachDynamicCompletionTo(TextField textField, Function<String, Collection<String>> possibleSuggestions) {
		TextFields.bindAutoCompletion(textField, req -> possibleSuggestions.apply(req.getUserText()));
	}
}

package milkman.ui.components;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.controlsfx.control.textfield.AutoCompletionBinding.ISuggestionRequest;
import org.controlsfx.control.textfield.TextFields;

import javafx.scene.control.TextField;
import javafx.util.Callback;
import lombok.RequiredArgsConstructor;
import milkman.domain.Environment.EnvironmentEntry;
import milkman.utils.controlfx.PartialAutoCompletion;


@RequiredArgsConstructor(onConstructor_={@Inject})
public class AutoCompleter {

	private final Supplier<List<EnvironmentEntry>> activeEnvironmentEntrySupplier;
	
	
	public void attachVariableCompletionTo(TextField textField) {
		new PartialAutoCompletion<>(textField, new Callback<ISuggestionRequest, Collection<String>>() {
			@Override
			public Collection<String> call(ISuggestionRequest req) {
				String userText = req.getUserText();
				return activeEnvironmentEntrySupplier.get().stream()
					.filter(e -> e.getName().contains(userText))
					.filter(e -> !e.getName().isBlank())
					.map(e -> e.getName())
					.collect(Collectors.toList());
			}
		});
	}
	
	public void attachStaticCompletionTo(TextField textField, Collection<String> possibleSuggestions) {
		TextFields.bindAutoCompletion(textField, possibleSuggestions);
	}
}

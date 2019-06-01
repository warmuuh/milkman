package milkman.utils.controlfx;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import lombok.RequiredArgsConstructor;
import lombok.Value;

public class PartialAutoCompletion<T> extends AutoCompletionTextFieldBinding<T> {

	public static Pattern PATTERN = Pattern.compile("\\{\\{([^\\}]*)(\\}\\})?");
	private TextField textField;

	public PartialAutoCompletion(TextField textField, Callback<ISuggestionRequest, Collection<T>> suggestionProvider) {
		super(textField, new PartialSuggestionProvider<>(textField, suggestionProvider));
		this.textField = textField;
	}

	@Override
	protected void completeUserInput(T completion) {
		if (completion != null) {
			Optional<Matcher> match = PartialAutoCompletion.findVariableInSelection(textField);
			match.ifPresent(m -> {
				textField.replaceText(m.start(), m.end(), "{{" + completion + "}}");
			});
		}
	}
	
	public static Optional<Matcher> findVariableInSelection(TextField tf){
		Matcher matcher = PartialAutoCompletion.PATTERN.matcher(tf.getText());

		while(matcher.find()) {
			if (tf.getSelection().getStart() >= matcher.start() 
					&& tf.getSelection().getEnd() <= matcher.end()) {
				return Optional.of(matcher);
			}
		}
		return Optional.empty();
	}
	
	@RequiredArgsConstructor
	private static class PartialSuggestionProvider<T> implements Callback<ISuggestionRequest, Collection<T>> {

		private final TextField textField;
		private final Callback<ISuggestionRequest, Collection<T>> suggestionProvider;
		
		@Override
		public Collection<T> call(ISuggestionRequest req) {
			Optional<Matcher> match = PartialAutoCompletion.findVariableInSelection(textField);
			return match.map(m -> suggestionProvider.call(new SimpleSuggestionRequest(m.group(1), req.isCancelled())))
						.orElse(Collections.emptyList());
		}
		
	}
	

	@Value
	private static class SimpleSuggestionRequest implements ISuggestionRequest {
		String userText;
		boolean cancelled;
	}
	

}

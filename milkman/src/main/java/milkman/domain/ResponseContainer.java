package milkman.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import lombok.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(include = As.PROPERTY, use = Id.CLASS)
public abstract class ResponseContainer {

	private List<ResponseAspect> aspects = new LinkedList<ResponseAspect>();

	
	@JsonIgnore
	private StatusInfoContainer statusInformations = new StatusInfoContainer();
	
	
	public <T extends ResponseAspect> Optional<T> getAspect(Class<T> aspectType) {
		return aspects.stream()
				.filter(aspectType::isInstance)
				.findAny()
				.map(a -> (T)a);
	}

	@Getter @EqualsAndHashCode
	public static class StyledText {
		private final String text;
		private final Optional<String> style;

		public StyledText(String text) {
			this(text, Optional.empty());
		}

		public StyledText(String text, String style) {
			this(text, Optional.of(style));
		}

		private StyledText(String text, Optional<String> style) {
			this.text = text;
			this.style = style;
		}
	}
}

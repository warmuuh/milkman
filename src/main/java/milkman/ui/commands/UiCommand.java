package milkman.ui.commands;

import lombok.Value;
import milkman.domain.RequestContainer;

public interface UiCommand {

	@Value
	public static class SaveRequestAsCommand implements UiCommand {
		RequestContainer request;
	}
	
	@Value
	public static class SaveRequestCommand implements UiCommand {
		RequestContainer request;
	}
	
	
	@Value
	public static class SubmitRequest implements UiCommand {
		RequestContainer request;
	}
	
	@Value
	public static class LoadRequest implements UiCommand {
		RequestContainer request;
	}
	
}

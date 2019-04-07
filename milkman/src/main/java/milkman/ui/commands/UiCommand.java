package milkman.ui.commands;

import lombok.Value;
import milkman.domain.Collection;
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
		String requestId;
	}

	@Value
	public static class SwitchToRequest implements UiCommand {
		RequestContainer request;
	}
	
	@Value
	public static class CloseRequest implements UiCommand {
		RequestContainer request;
	}
	
	@Value
	public static class DeleteRequest implements UiCommand {
		RequestContainer request;
		Collection collection;
	}
	
	@Value
	public static class RenameRequest implements UiCommand {
		RequestContainer request;
	}
	
	@Value
	public static class NewRequest implements UiCommand {
	}
	
	@Value
	public static class DeleteCollection implements UiCommand {
		Collection collection;
	}
	
}

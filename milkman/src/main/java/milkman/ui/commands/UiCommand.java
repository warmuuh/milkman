package milkman.ui.commands;

import lombok.Value;
import milkman.domain.Collection;
import milkman.domain.RequestContainer;
import milkman.ui.plugin.CustomCommand;

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
	public static class SaveActiveRequest implements UiCommand {
		
	}
	
	@Value
	public static class SubmitRequest implements UiCommand {
		RequestContainer request;
	}
	@Value
	public static class SubmitCustomCommand implements UiCommand {
		RequestContainer request;
		CustomCommand command;
	}
	
	@Value
	public static class SubmitActiveRequest implements UiCommand {
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
		CloseType type;
		public enum CloseType {
			CLOSE_THIS,
			CLOSE_ALL,
			CLOSE_RIGHT,
			CLOSE_OTHERS
		}
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
	public static class RenameActiveRequest implements UiCommand {
	}
	@Value
	public static class NewRequest implements UiCommand {
	}
	
	@Value
	public static class DeleteCollection implements UiCommand {
		Collection collection;
	}
	@Value
	public static class RenameCollection implements UiCommand {
		Collection collection;
	}
	
	@Value
	public static class ExportRequest implements UiCommand {
		RequestContainer request;
	}
	
	@Value
	public static class ExportCollection implements UiCommand {
		Collection collection;
	}
	
}

package milkman.ui.commands;

import javafx.scene.Node;
import lombok.Data;
import lombok.Value;
import milkman.domain.Collection;
import milkman.domain.Folder;
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
	public static class CancelActiveRequest implements UiCommand {
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
	public static class CloseActiveRequest implements UiCommand {
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
		Node quickSelectNode;
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

	@Data
	public static class AddFolder implements UiCommand {
		private final Collection collection;
		private final Folder folder;
		
		public AddFolder(Collection collection) {
			this.collection = collection;
			this.folder = null;
		}
		
		public AddFolder(Folder folder) {
			this.collection = null;
			this.folder = folder;
		}
		
	}
	

	@Value
	public static class DeleteFolder implements UiCommand {
		Collection collection;
		Folder folder;
	}
}

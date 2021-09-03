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
	class SaveRequestAsCommand implements UiCommand {
		RequestContainer request;
	}
	
	@Value
	class SaveRequestCommand implements UiCommand {
		RequestContainer request;
	}
	@Value
	class SaveActiveRequest implements UiCommand {
		
	}
	
	@Value
	class SubmitRequest implements UiCommand {
		RequestContainer request;
	}
	@Value
	class SubmitCustomCommand implements UiCommand {
		RequestContainer request;
		CustomCommand command;
	}
	
	@Value
	class SubmitActiveRequest implements UiCommand {
	}
	@Value
	class CancelActiveRequest implements UiCommand {
	}
	@Value
	class CancelHighlight implements UiCommand {
	}
	@Value
	class LoadRequest implements UiCommand {
		String requestId;
	}

	@Value
	class SwitchToRequest implements UiCommand {
		RequestContainer request;
	}
	@Value
	class HighlightVariables implements UiCommand {
	}

	@Value
	class CloseRequest implements UiCommand {
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
	class DuplicateRequest implements UiCommand {
		RequestContainer request;
	}

	@Value
	class SelectRequest implements UiCommand {
		RequestContainer request;
	}

	@Value
	class CloseActiveRequest implements UiCommand {
	}
	
	@Value
	class DeleteRequest implements UiCommand {
		RequestContainer request;
		Collection collection;
	}
	
	@Value
	class RenameRequest implements UiCommand {
		RequestContainer request;
	}
	
	@Value
	class RenameActiveRequest implements UiCommand {
	}
	@Value
	class NewRequest implements UiCommand {
		Node quickSelectNode;
	}
	
	@Value
	class DeleteCollection implements UiCommand {
		Collection collection;
	}
	@Value
	class RenameCollection implements UiCommand {
		Collection collection;
	}
	
	@Value
	class ExportRequest implements UiCommand {
		RequestContainer request;
	}
	
	@Value
	class ExportCollection implements UiCommand {
		Collection collection;
	}

	@Data
	class AddFolder implements UiCommand {
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
	class DeleteFolder implements UiCommand {
		Collection collection;
		Folder folder;
	}
}

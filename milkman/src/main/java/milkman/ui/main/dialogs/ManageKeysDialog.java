package milkman.ui.main.dialogs;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import milkman.domain.KeySet;
import milkman.domain.KeySet.KeyEntry;
import milkman.ui.components.JfxTableEditor;
import milkman.ui.plugin.KeyEditor;
import milkman.utils.ObjectUtils;
import milkman.utils.fxml.FxmlUtil;
import milkman.utils.fxml.facade.DialogLayoutBase;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static milkman.utils.fxml.facade.FxmlBuilder.cancel;
import static milkman.utils.fxml.facade.FxmlBuilder.label;

public class ManageKeysDialog {

	JfxTableEditor<KeyEntry> editor;
	private Dialog dialog;
	private List<KeyEditor> keyEditors;

	public void showAndWait(KeySet keySet, List<KeyEditor> editors) {
		this.keyEditors = editors;
		var content = new ManageKeysDialogFxml(this);

		editor.enableAddition(this::addNewEntry);
		editor.addColumn("Name", KeyEntry::getName, KeyEntry::setName);
		editor.addReadOnlyColumn("Type", KeyEntry::getType);
		editor.addReadOnlyColumn("Value", KeyEntry::getPreview);
		editor.addCustomAction(FontAwesomeIcon.PENCIL, this::editKey);
		editor.addCustomAction(FontAwesomeIcon.CLONE, this::copyKey);
		editor.addDeleteColumn("Delete");
		editor.setItems(keySet.getEntries(), Comparator.comparing(KeyEntry::getName));
		editor.setRowToStringConverter(e -> e.getName() + ": " + e.getValue());



		dialog = FxmlUtil.createDialog(content);
		dialog.showAndWait();
	}

	private void copyKey(KeyEntry keyEntry) {
		var copy = ObjectUtils.deepClone(keyEntry);
		copy.setId(UUID.randomUUID().toString());
		copy.setName(copy.getName() + " (copy)");
		editor.addNewItemManually(copy);

	}

	private void editKey(KeyEntry keyEntry) {
		keyEditors.stream()
				.filter(editor -> editor.supportsKeyType(keyEntry))
				.findAny()
				.ifPresent(editor -> showKeyEditor(keyEntry, editor));
	}

	private void showKeyEditor(KeyEntry keyEntry, KeyEditor editor) {
		var dialog = new EditKeyDialog();
		dialog.showAndWait(keyEntry, editor);
	}

	public KeyEntry addNewEntry() {
		var selectValueDialog = new SelectValueDialog();
		var keyEditorNames = keyEditors.stream().map(KeyEditor::getName).collect(Collectors.toList());
		selectValueDialog.showAndWait("New Key", "Select new key type", Optional.empty(), keyEditorNames);
		if (!selectValueDialog.isCancelled()) {
			return keyEditors.stream()
				.filter(ke -> ke.getName().equals(selectValueDialog.getInput()))
				.findAny()
				.map(keyEditor -> {
					var newEntry = keyEditor.getNewKeyEntry();
					editKey(newEntry);
					return newEntry;
				})
				.orElse(null);
		}
		return null;
	}


	public void onClose() {
		dialog.close();
	}


	public static class ManageKeysDialogFxml extends DialogLayoutBase {

		public ManageKeysDialogFxml(ManageKeysDialog controller){
			setHeading(label("Edit Key-Set"));

			var editor = controller.editor = new JfxTableEditor<>();
			editor.setMinHeight(500);
			editor.setMinWidth(800);
			setBody(editor);

			Button close = cancel(controller::onClose, "Close");
			close.getStyleClass().add("dialog-accept");
			setActions(close);
		}

	}

}

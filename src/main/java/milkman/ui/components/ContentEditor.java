package milkman.ui.components;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.jfoenix.controls.JFXComboBox;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import milkman.utils.fxml.GenericBinding;

/**
 * <HBox styleClass="contentEditor-header">
		<Label text="Content Type:"></Label>
		<JFXComboBox value="Plain Text">
			<items>
				<FXCollections fx:factory="observableArrayList">
					<String fx:value="Plain Text" />
					<String fx:value="Json" />
				</FXCollections>
			</items>
		</JFXComboBox>
	</HBox>
	<TextArea fx:id="textArea" VBox.vgrow="ALWAYS"></TextArea>
 * @author peter
 *
 * @param <T>
 */
public class ContentEditor extends VBox {

	TextArea textArea;

	GenericBinding<Object, String> contentBinding;
	
	
	public ContentEditor() {
		getStyleClass().add("contentEditor");
		
		JFXComboBox<String> highlighters = new JFXComboBox<String>();
		highlighters.getItems().add("Plain Text");
		highlighters.getItems().add("Json");
		highlighters.setValue("Plain Text");
		
		HBox header = new HBox(new Label("Content Type:"), highlighters);
		header.getStyleClass().add("contentEditor-header");
		
		getChildren().add(header);
		textArea = new TextArea();
		VBox.setVgrow(textArea, Priority.ALWAYS);
		getChildren().add(textArea);
	}
	
	public void setEditable(boolean editable) {
		textArea.setEditable(editable);
	}
	
	
	public void setContent(Supplier<String> getter, Consumer<String> setter) {		
		if (contentBinding != null) {
			textArea.textProperty().unbindBidirectional(contentBinding);
		}
		contentBinding = GenericBinding.of(o -> getter.get(), (o,v) -> setter.accept(v), null);
		textArea.textProperty().bindBidirectional(contentBinding);
	}


}

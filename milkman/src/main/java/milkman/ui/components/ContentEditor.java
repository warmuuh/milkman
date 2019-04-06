package milkman.ui.components;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;
import org.reactfx.value.Val;
import org.reactfx.value.Var;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import lombok.val;
import milkman.ui.plugin.ContentTypePlugin;
import milkman.utils.fxml.GenericBinding;

/**
 * @author peter
 *
 * @param <T>
 */
public class ContentEditor extends VBox {

	private static final String DEFAULT_CONTENTTYPE = "text/plain";

	CodeArea codeArea;

	GenericBinding<Object, String> contentBinding;

	private JFXComboBox<ContentTypePlugin> highlighters;

	JFXButton format;
	
	
	
	public ContentEditor() {
		getStyleClass().add("contentEditor");
		
		highlighters = new JFXComboBox<ContentTypePlugin>();
		highlighters.setConverter(new StringConverter<ContentTypePlugin>() {
			@Override
			public String toString(ContentTypePlugin object) {
				return object.getName();
			}
			@Override
			public ContentTypePlugin fromString(String string) {
				return null;
			}
		});
		
		
		format = new JFXButton("Format");
		format.setVisible(false);
		
		format.setOnAction(e -> formatCode());
		
		HBox header = new HBox(new Label("Content Type:"), highlighters, format);
		header.getStyleClass().add("contentEditor-header");
		
		getChildren().add(header);
		codeArea = new CodeArea();
		
		highlighters.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
			codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText()));
			if (n != null)
				format.setVisible( n.supportFormatting());
		});
		
		codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
		Subscription cleanupWhenNoLongerNeedIt = codeArea
				 .multiPlainChanges()
				 .successionEnds(Duration.ofMillis(500))
				 .subscribe(ignore -> codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText())));
		
		VBox.setVgrow(codeArea, Priority.ALWAYS);
		getChildren().add(codeArea);
	}
	
	private void formatCode() {
		if (highlighters.getValue() != null && highlighters.getValue().supportFormatting())
			codeArea.replaceText(highlighters.getValue().formatContent(codeArea.getText()));
	}

	private StyleSpans<? extends Collection<String>> computeHighlighting(String text) {
		if (highlighters.getValue() != null)
			return highlighters.getValue().computeHighlighting(text);
		else
			return noHighlight(text);
	}

	private StyleSpans<? extends Collection<String>> noHighlight(String text) {
		val b = new StyleSpansBuilder<Collection<String>>();
		b.add(Collections.singleton("plain"), text.length());
		return b.create();
	}

	public void setEditable(boolean editable) {
		codeArea.setEditable(editable);
	}
	
	public void setContentTypePlugins(List<ContentTypePlugin> plugins) {
		highlighters.getItems().addAll(plugins);
		//set plain highlighter as default:
		plugins.stream()
		.filter(p -> p.getContentType().equals(DEFAULT_CONTENTTYPE))
		.findAny().ifPresent(t -> {
			format.setVisible(t.supportFormatting());
			highlighters.setValue(t);
		});
	}
	
	
	public void setContent(Supplier<String> getter, Consumer<String> setter) {		
		
//		if (contentBinding != null) {
//			Bindings.unbindBidirectional(codeAreaTextBinding, contentBinding);
//		}
//		contentBinding = GenericBinding.of(o -> getter.get(), (o,v) -> setter.accept(v), null);
//		codeAreaTextBinding = Var.mapBidirectional(contentBinding,  s -> s, s->s);
		
		String curValue = getter.get();
		codeArea.replaceText(curValue != null ? curValue : "");
		codeArea.textProperty().addListener((obs, o, n)->{
			setter.accept(n);
		});
		
	}

}

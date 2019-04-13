package milkman.ui.components;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import lombok.val;
import milkman.ui.main.options.CoreApplicationOptionsProvider;
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

	private HBox header;
	
	private TextField searchField;
	
	
	public ContentEditor() {
		getStyleClass().add("contentEditor");
		
		setupHeader();
		setupCodeArea();
		setupSearch();
		
		StackPane.setAlignment(searchField, Pos.TOP_RIGHT);
		StackPane contentPane = new StackPane(new VirtualizedScrollPane(codeArea), searchField);
		VBox.setVgrow(contentPane, Priority.ALWAYS);
		
		getChildren().add(contentPane);
	}


	private void setupCodeArea() {
		codeArea = new CodeArea();
		
		
		codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
		Subscription cleanupWhenNoLongerNeedIt = codeArea
				 .multiPlainChanges()
				 .successionEnds(Duration.ofMillis(500))
				 .subscribe(ignore -> highlightCode());
		
		
		val keyCombination = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
		codeArea.setOnKeyPressed(e -> {
			if (keyCombination.match(e)) {
				focusSearch();
			}
		});
	}


	private void setupSearch() {
		searchField = new TextField();
		searchField.focusedProperty().addListener((obs, o, n) -> {
			if (n != null && n == false) {
				hideSearch();
			}
		});
		searchField.setOnKeyReleased(e -> {
			if (e.getCode() == KeyCode.ESCAPE) {
				hideSearch();
			}
		});
		
		searchField.setVisible(false);
		searchField.setPromptText("Search");
		searchField.setMaxWidth(200);
		searchField.setOnAction(e -> {
			if (searchField.getText().length() > 0) {
				int sIdx = codeArea.getText().indexOf(searchField.getText(), codeArea.getCaretPosition());
				if (sIdx < 0) {
					//wrap search:
					sIdx = codeArea.getText().substring(0, codeArea.getCaretPosition()).indexOf(searchField.getText());
				}
				if (sIdx >= 0) {
					codeArea.selectRange(sIdx, sIdx + searchField.getText().length());
					codeArea.requestFollowCaret();
				}
				
			}
		});
	}


	private void hideSearch() {
		searchField.setVisible(false);
		codeArea.requestFocus();
	}


	private void focusSearch() {
		searchField.setVisible(true);
		searchField.requestFocus();
	}


	private void setupHeader() {
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
		
		highlighters.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
			highlightCode();
			if (n != null)
				format.setVisible( n.supportFormatting());
		});
		
		
		format = new JFXButton("Format");
		format.setVisible(false);
		
		format.setOnAction(e -> formatCode());
		
		header = new HBox(new Label("Content Type:"), highlighters, format);
		header.getStyleClass().add("contentEditor-header");
		
		getChildren().add(header);
	}

	
	public void setHeaderVisibility(boolean isVisible) {
		if (isVisible && !getChildren().contains(header)) {
			getChildren().add(header);
		} else {
			getChildren().remove(header);
		}
	}
	
	private void highlightCode() {
		codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText()));
	}
	
	private void formatCode() {
		if (highlighters.getValue() != null && highlighters.getValue().supportFormatting()) {
			codeArea.replaceText(highlighters.getValue().formatContent(codeArea.getText()));
			highlightCode();			
		}
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
		String contentType = DEFAULT_CONTENTTYPE;
		setActiveContentType(plugins, contentType);
	}

	private void setActiveContentType(List<ContentTypePlugin> plugins, String contentType) {
		plugins.stream()
		.filter(p -> p.getContentType().equals(contentType))
		.findAny().ifPresent(t -> {
			format.setVisible(t.supportFormatting());
			highlighters.setValue(t);
		});
	}
	
	public void setContentType(String contentType) {
		setActiveContentType(highlighters.getItems(), contentType);
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
		
		if (CoreApplicationOptionsProvider.options().isAutoformatContent()) {
			Platform.runLater(this::formatCode);
		}
	}

}

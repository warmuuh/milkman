package milkman.ui.components;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyledTextArea;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.Data;
import lombok.Value;

/**
 * Demonstrates the usage of {@link StyledTextArea#paragraphGraphicFactoryProperty()}.
 */
public class LineIndicatorDemo extends Application {

	CollapsableRange rootRange;
	private CodeArea codeArea;
    
	
    @Override
    public void start(Stage primaryStage) {
        codeArea = new CodeArea();

        IntFunction<Node> numberFactory = LineNumberFactory.get(codeArea);
        IntFunction<Node> arrowFactory = new ArrowFactory();
        IntFunction<Node> graphicFactory = line -> {
            var collapseSign = arrowFactory.apply(line);
            
			HBox hbox = new HBox(numberFactory.apply(line));
			hbox.setPrefWidth(40);
			if (collapseSign != null)
				hbox.getChildren().add(collapseSign);
			
            hbox.setAlignment(Pos.CENTER_LEFT);
            return hbox;
        };
        codeArea.setParagraphGraphicFactory(graphicFactory);
        replaceText("This is a paragraph of text.\n"
        		+ "(This is a nested collapsable paragraph of text.\n"
        		+ "This is a paragraph of text.)\n"
        		+ "This is a paragraph of text.\n"
        		+ "\n"
        		+ "This is a paragraph of text.\n"
        		+ "This is a paragraph of text.\n"
        		+ "This is a paragraph of text.\n"
        		+ "This is a paragraph of text.\n"
        		+ "\n"
        		+ "This is a paragraph of text.\n"
        		+ "This is a paragraph of text.\n"
        		+ "This is a paragraph of text.\n"
        		+ "This is a paragraph of text.\n"
        		+ "\n"
        		+ "Try it.");
        codeArea.moveTo(0, 0);

        var scene = new Scene(new StackPane(codeArea), 600, 400);
        scene.getStylesheets().add("/themes/milkman.css");
		primaryStage.setScene(scene);
        
        primaryStage.show();
    }
    
    
    @Data
    private abstract static class ContentRange {
    	protected final ContentRange prevRange;
    	
    	public int getStartLine(){
    		return prevRange == null ? 0 : prevRange.getEndLine();
    	}
    	
    	public int getEndLine() {
    		return getStartLine() + getContainedLines();
    	}
    	
    	public abstract int getContainedLines();
    	public abstract void appendToString(StringBuilder b);

    }
    
    @Data
    private static class TextRange extends ContentRange {
    	private final String text;
    	private final int containedLines;
    	
    	public TextRange(ContentRange prevRange, String text) {
			super(prevRange);
			this.text = text;
			containedLines = StringUtils.countMatches(text, '\n');
		}
    	
    	public void appendToString(StringBuilder b) {
    		b.append(text);
    	}
    }
    
    @Data
    private static class CollapsableRange extends ContentRange {
    	private final boolean isRoot;
    	private boolean collapsed;

    	private List<ContentRange> children = new LinkedList<>();
    	
    	public CollapsableRange(ContentRange prevRange, boolean isRoot) {
			super(prevRange);
			this.isRoot = isRoot;
		}
    	
    	public void setCollapsed(boolean value){
    		collapsed = value;
    	}
    	
    	public void addChildren(ContentRange range) {
    		children.add(range);
    	}
    	
    	public void appendToString(StringBuilder b) {
    		if (collapsed) {
    			b.append("...\n");
    		} else {
    			for (ContentRange range : children) {
					range.appendToString(b);
				}
    		}
    	}

    	public int getContainedLines() {
    		if (collapsed)
    			return 1;
    		int sum = 0;
    		for (ContentRange range : children) {
    			sum += range.getContainedLines();
			}
			return sum;
    	}
    	
    	public int getStartLine() {
    		return isRoot ? -1 : super.getStartLine();
    	}

    }
    
    public void replaceText(String text) {
    	rootRange = new CollapsableRange(null, true);
    	String[] splits = StringUtils.splitByWholeSeparator(text, "\n\n");
    	ContentRange prevRange = null;
    	
    	for (int i = 0; i < splits.length; i++) {
			String split = splits[i];
			
			var curRange = new CollapsableRange(prevRange, false);
    		parseParagraph(curRange, prevRange, split + "\n");
    		rootRange.addChildren(curRange);
    		prevRange = curRange;
    		if (i < splits.length -1) { // not last one
    			var paragraphSeparator = new TextRange(curRange, "\n");
    			prevRange = paragraphSeparator;
				rootRange.addChildren(paragraphSeparator);
    		}
		}
    	redrawText();
    }


	private void parseParagraph(CollapsableRange curRange, ContentRange prevRange, String paragraph) {
		String[] splits = StringUtils.split(paragraph, "()");
		if (splits.length == 1) {
    		var containedNewLines = StringUtils.countMatches(paragraph, '\n');
			curRange.addChildren(new TextRange(prevRange, paragraph));
		} else {
			//for testing, assume, that there is one matching pair, e.g. 3 splits
    		var split1Lines = StringUtils.countMatches(splits[0], '\n');
			var range1 = new TextRange(prevRange, splits[0]);
			curRange.addChildren(range1);
			
			var split2Lines = StringUtils.countMatches(splits[1], '\n');
			var nestedRange = new CollapsableRange(range1, false);
			nestedRange.addChildren(new TextRange(range1, "(" + splits[1] + ")\n"));
			curRange.addChildren(nestedRange);
			
			var split3Lines = StringUtils.countMatches(splits[2], '\n');
			curRange.addChildren(new TextRange(nestedRange, splits[2].trim() + "\n"));
		}
	}
    
    
    private void redrawText() {
    	StringBuilder b = new StringBuilder();
    	rootRange.appendToString(b);
    	codeArea.replaceText(b.toString());
	}

    private Optional<CollapsableRange> lookupCollapsableRangeInStartLineIdx(int lineNumber, ContentRange curContentRange){
    	if(!(curContentRange instanceof CollapsableRange)) {
    		return Optional.empty();
    	}
    	var collapsable = (CollapsableRange) curContentRange;
    	if (collapsable.getStartLine() == lineNumber)
    		return Optional.of(collapsable);
    	
    	if (!collapsable.isCollapsed()) {
    		for (ContentRange childRange : collapsable.getChildren()) {
    			var childMatch = lookupCollapsableRangeInStartLineIdx(lineNumber, childRange);
    			if (childMatch.isPresent())
    				return childMatch;
    		}
    	}
    	return Optional.empty();
    }

	private class ArrowFactory implements IntFunction<Node> {

        @Override
        public Node apply(int lineNumber) {
//        	FontAwesomeIconView empty = new FontAwesomeIconView(FontAwesomeIcon.PLUS_SQUARE);
//        	empty.setVisible(false);
        	return lookupCollapsableRangeInStartLineIdx(lineNumber, rootRange)
        		.map(r -> {
        			FontAwesomeIconView view; 
					if (r.isCollapsed()) {
						view = new FontAwesomeIconView(FontAwesomeIcon.PLUS_SQUARE);
					} else {
						view = new FontAwesomeIconView(FontAwesomeIcon.MINUS_SQUARE);
					}
					view.setOnMouseClicked(e -> {
						r.setCollapsed(!r.isCollapsed());
						redrawText();
					});
					view.setStyleClass("handCursor");
					return view;
				}).orElse(null);
        }
    }
	

    public static void main(String[] args) {
        launch(args);
    }

}
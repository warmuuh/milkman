package milkman.ui.components;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.function.IntFunction;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.LineNumberFactory;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import lombok.Data;

public class CodeFoldingContentEditor extends ContentEditor {

	
	
	ContentRange rootRange;

	
	
    @Override
	protected void setupParagraphGraphics() {
    	 IntFunction<Node> numberFactory = LineNumberFactory.get(codeArea);
         IntFunction<Node> arrowFactory = new FoldOperatorFactory();
         IntFunction<Node> graphicFactory = line -> {
             var collapseSign = arrowFactory.apply(line);
             
 			HBox hbox = new HBox(numberFactory.apply(line));
 			hbox.setPrefWidth(50);
 			if (collapseSign != null)
 				hbox.getChildren().add(collapseSign);
 			
             hbox.setAlignment(Pos.CENTER_LEFT);
             return hbox;
         };
         codeArea.setParagraphGraphicFactory(graphicFactory);
	}

    
    protected void replaceText(String text) {
    	if (getCurrentContenttypePlugin().supportFolding())
    	{
    		rootRange = getCurrentContenttypePlugin().computeFolding(text);
        	redrawText();
    	} else {
    		super.replaceText(text);
    	}
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

    private void redrawText() {
    	StringBuilder b = new StringBuilder();
    	rootRange.appendToString(b);
    	codeArea.replaceText(b.toString());
	}
    
	private class FoldOperatorFactory implements IntFunction<Node> {

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
	
	@Data
	public abstract static class ContentRange {
		protected final ContentRange prevRange;

		public int getStartLine() {
			return prevRange == null ? 0 : prevRange.getEndLine();
		}

		public int getEndLine() {
			return getStartLine() + getContainedLines();
		}

		public abstract int getContainedLines();

		public abstract void appendToString(StringBuilder b);

	}

	@Data
	public static class TextRange extends ContentRange {
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
	public static class CollapsableRange extends ContentRange {
		private final boolean isRoot;
		private boolean collapsed;

		private List<ContentRange> children = new LinkedList<>();

		public CollapsableRange(ContentRange prevRange, boolean isRoot) {
			super(prevRange);
			this.isRoot = isRoot;
		}

		public void setCollapsed(boolean value) {
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

	public static class CodeFoldingBuilder {
    	private final String text;
    	private final String palceholder;
    	private final Stack<CollapsableRange> rangeStack;
    	private int curIdx = 0;

		public CodeFoldingBuilder(String text, String palceholder) {
			this.text = text;
			this.palceholder = palceholder;
			rangeStack = new Stack<>();
			rangeStack.add(new CollapsableRange(null, true));
		}

		/**
		 * will add everything from current idx to given idx as text node and add a new collapsable to the stack
		 * @param nextIdx
		 */
		public void startRange(int nextIdx){
			ContentRange prev = addLeftOverTextToCurrentRange(nextIdx);

			CollapsableRange newCollapsable = new CollapsableRange(prev, false);
			rangeStack.peek().addChildren(newCollapsable);
			rangeStack.add(newCollapsable);
		}

		private ContentRange addLeftOverTextToCurrentRange(int nextIdx) {
			ContentRange prev = null;
			if (rangeStack.peek().getChildren().size() > 0)
				prev = rangeStack.peek().getChildren().get( rangeStack.peek().getChildren().size() -1);

			if (curIdx < nextIdx) {
				prev = new TextRange(prev, text.substring(curIdx, nextIdx));
				rangeStack.peek().addChildren(prev);
			}
			curIdx = nextIdx;
			return prev;
		}

		/**
		 * will close the current collapsable and pop it from stack
		 * @param nextIdx
		 */
		public void endRange(int nextIdx){
			ContentRange prev = addLeftOverTextToCurrentRange(nextIdx);

			//just as a guard, dont pop the root
			if (rangeStack.size() > 1)
				rangeStack.pop();
		}

		public CollapsableRange build(){
			ContentRange prev = addLeftOverTextToCurrentRange(text.length());
			return rangeStack.get(0);
		}

	}
}

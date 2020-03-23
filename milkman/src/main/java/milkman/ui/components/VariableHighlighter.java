/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package milkman.ui.components;

import com.jfoenix.controls.JFXPopup;
import com.jfoenix.utils.JFXUtilities;
import com.sun.javafx.geom.RectBounds;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.text.TextLayout;
import com.sun.javafx.scene.text.TextLine;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.ScrollEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.WindowEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import milkman.ctrl.VariableResolver;
import milkman.ctrl.VariableResolver.VariableData;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.Paragraph;
import org.reactfx.collection.LiveList;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

import static milkman.utils.fxml.FxmlBuilder.*;


/**
 * JFXHighlighter is used to highlight Text and LabeledText nodes
 * (in a specific {@link Parent}) that matches the user query.
 *
 * @author Shadi Shaheen
 * @version 1.0
 * @since 2018-03-22
 */
@RequiredArgsConstructor
public class VariableHighlighter {

    private final VariableResolver variableResolver;
    private final Pattern tagPattern = Pattern.compile("[\\{]{2}([^}]+)[\\}]{2}");

    private Parent parent;
    private HashMap<Node, List<Rectangle>> boxes = new HashMap<>();

    private Method textLayoutMethod;
    private Field parentChildrenField;
    private JFXPopup popup;

    {
        try {
            textLayoutMethod = Text.class.getDeclaredMethod("getTextLayout");
            textLayoutMethod.setAccessible(true);
            parentChildrenField = Parent.class.getDeclaredField("children");
            parentChildrenField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * highlights the matching text in the specified pane
     * @param pane node to search into its text
     */
    public synchronized void highlight(Parent pane) {
        if (this.parent != null && !boxes.isEmpty()) {
            clear();
        }

        this.parent = pane;

        ArrayList<Rectangle> allRectangles = new ArrayList<>(processAllTextNodes(pane));

        allRectangles.addAll(processCodeAreas(parent));

        JFXUtilities.runInFXAndWait(()-> getParentChildren(pane).addAll(allRectangles));
    }

    private List<Rectangle> processCodeAreas(Parent pane) {
        var nodes = pane.lookupAll("ContentEditor");
        ArrayList<Rectangle> result = new ArrayList<>();


        for (Node node : nodes) {
            ArrayList<TextBoundingBox> bboxes = new ArrayList<>();
            var area = ((ContentEditor) node).getCodeArea();
            LiveList<Paragraph<Collection<String>, String, Collection<String>>> visibleParagraphs = area.getVisibleParagraphs();
            int visibleParIdx = 0;
            for (Paragraph visiblePar : visibleParagraphs) {
                int parIdx = area.visibleParToAllParIndex(visibleParIdx);
                var parMatcher = tagPattern.matcher(visiblePar.getText());
                if (parMatcher.find()){
//                    var bounds = area.getVisibleParagraphBoundsOnScreen(visibleParIdx);
                    var matchStartIdxAbs = area.getAbsolutePosition(parIdx, parMatcher.start());
                    var bounds = area.getCharacterBoundsOnScreen(matchStartIdxAbs, matchStartIdxAbs + parMatcher.group().length());
                    bounds.ifPresent(b -> {
                        var localBounds = parent.sceneToLocal(area.localToScene(area.screenToLocal(b)));
                        bboxes.add(new TextBoundingBox(parMatcher.group(1),
                                localBounds.getMinX(), localBounds.getMinY(), localBounds.getWidth(), localBounds.getHeight()));
                    });

                }
                visibleParIdx++;
            }
            var rectangles = getRectangles(area, bboxes);
            if (rectangles.size() > 0){
                result.addAll(rectangles);
                boxes.put(area, rectangles);
            }
        }

        return result;
    }

    private List<Rectangle> processAllTextNodes(Parent pane) {
        ArrayList<Rectangle> result = new ArrayList<>();
        Set<Node> nodes = getTextNodes(pane);
        for (Node node : nodes) {
            Text text = ((Text) node);
            var matcher = tagPattern.matcher(text.getText());
            if (matcher.find() && NodeHelper.isTreeVisible(node)) {
                ArrayList<Rectangle> rectangles = getRectangles(text);
                result.addAll(rectangles);
                boxes.put(node, rectangles);
            }
        }
        return result;
    }

    private ArrayList<Rectangle> getRectangles(Text text) {
        ArrayList<TextBoundingBox> boundingBoxes = getMatchingBounds(text);
        ArrayList<Rectangle> rectangles = getRectangles(text, boundingBoxes);
        return rectangles;
    }

    private ArrayList<Rectangle> getRectangles(Node node, ArrayList<TextBoundingBox> boundingBoxes) {
        ArrayList<Rectangle> rectangles = new ArrayList<>();
        for (TextBoundingBox boundingBox : boundingBoxes) {
            var data = variableResolver.getVariableData(boundingBox.getText());

            HighLightRectangle rect = new HighLightRectangle(node);
            rect.setCacheHint(CacheHint.SPEED);
            rect.setCache(true);
//            rect.setMouseTransparent(true);
            rect.setBlendMode(BlendMode.MULTIPLY);

            rect.setArcWidth(10);
            rect.setArcHeight(10);

            if (data.isNewVariable()){
                rect.getStyleClass().add("variable-new");
//                rect.strokeProperty().bind(newVarPaintProperty());
            } else {
                rect.getStyleClass().add("variable");
//                rect.fillProperty().bind(existingVarPaintProperty());
            }



            rect.setManaged(false);
            rect.setX(boundingBox.getMinX());
            rect.setY(boundingBox.getMinY());
            rect.setWidth(boundingBox.getWidth());
            rect.setHeight(boundingBox.getHeight());



            rect.setOnMouseClicked(e -> {
                var popup = showVariablePopup(boundingBox, data);
                popup.addEventHandler(WindowEvent.WINDOW_HIDDEN, evt -> Platform.runLater(VariableHighlighter.this::clear));
            });

            rectangles.add(rect);
        }
        return rectangles;
    }

    private JFXPopup showVariablePopup(TextBoundingBox boundingBox, VariableData varData) {
        var vBox = new VboxExt();
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(10));

        String title = varData.isNewVariable() ? "New Variable" : "Modify Variable";

        vBox.add(label(title)).getStyleClass().add("popup-title");
        vBox.add(hbox(label("Name"), label(varData.getName()))).setSpacing(20);
        vBox.add(hbox(label("Environment"), label(varData.getEnvironmentName()))).setSpacing(20);
        vBox.add(label("Value"));

        var replTxt = vBox.add(new TextArea(varData.getValue()));
        replTxt.setMaxHeight(50);

        popup = new JFXPopup(vBox);

        if (varData.isWriteable()){
            String btnText = varData.isNewVariable() ? "Create Variable" : "Modify Variable";
            var btn = vBox.add(new HboxExt().add(submit(() -> {
                varData.writeNewValue(replTxt.getText());
                popup.hide();
            }, btnText)));
            btn.getStyleClass().add("primary-button");
        }



        popup.show(this.parent, JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.LEFT);
        return popup;
    }

    private class HighLightRectangle extends Rectangle{
        // add listener to remove the current rectangle if text was changed
        private InvalidationListener listener;

        public HighLightRectangle(Node node) {
            if (node instanceof Text){
                Text text = (Text) node;
                listener = observable -> clear(node);
                text.textProperty().addListener(new WeakInvalidationListener(listener));
                text.localToSceneTransformProperty().addListener(new WeakInvalidationListener(listener));
            }
            if (node instanceof CodeArea){
                CodeArea codeArea = (CodeArea) node;
                listener = observable -> clear(node);
                codeArea.addEventFilter(ScrollEvent.ANY, evt -> listener.invalidated(null));
                codeArea.localToSceneTransformProperty().addListener(new WeakInvalidationListener(listener));
                codeArea.textProperty().addListener(new WeakInvalidationListener(listener));

//                codeArea.textProperty().addListener((obs, o, n) -> System.out.println("test"));
//                var parent = (VirtualizedScrollPane) codeArea.getParent();
//                parent.onScrollProperty().addListener(new WeakInvalidationListener(listener));
//                parent.localToSceneTransformProperty().addListener(new WeakInvalidationListener(listener));
//                parent.addEventFilter(ScrollEvent.ANY, evt -> listener.invalidated(null));

            }
        }

        private void clear(Node node) {
            final List<Rectangle> rectangles = boxes.get(node);
            if(rectangles != null && !rectangles.isEmpty())
                Platform.runLater(() -> getParentChildren(parent).removeAll(rectangles));
        }
    }

    private Set<Node> getTextNodes(Parent pane) {
        Set<Node> labeledTextNodes = pane.lookupAll("LabeledText");
        Set<Node> textNodes = pane.lookupAll("Text");
        Set<Node> nodes = new HashSet<>();
        nodes.addAll(labeledTextNodes);
        nodes.addAll(textNodes);
        return nodes;
    }

    private ObservableList<Node> getParentChildren(Parent parent){
        try {
            return (ObservableList<Node>) parentChildrenField.get(parent);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ArrayList<TextBoundingBox> getMatchingBounds(Text text) {
        // find local text bounds in parent
        Bounds textBounds = parent.sceneToLocal(text.localToScene(text.getBoundsInLocal()));

        ArrayList<TextBoundingBox> rectBounds = new ArrayList<>();

        TextLayout textLayout = null;
        try {
            textLayout = (TextLayout) textLayoutMethod.invoke(text);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        TextLine[] lines = textLayout.getLines();
        // handle matches in all lines
        for (int i = 0; i < lines.length; i++) {
            TextLine line = lines[i];
            String lineText = text.getText().substring(line.getStart(), line.getStart() + line.getLength());

            var matcher = tagPattern.matcher(lineText);
            RectBounds lineBounds = (line.getBounds());

            // compute Y layout
            double height = Math.round(lineBounds.getMaxY()) - Math.round(lineBounds.getMinY());
            double startY = height * i;

            // handle multiple matches in one line
            while (matcher.find()) {
                // compute X layout
                var matchedText = lineText.substring(matcher.start(), matcher.end());
                Text temp = new Text(matchedText);
                temp.setFont(text.getFont());
                temp.applyCss();
                double width = temp.getLayoutBounds().getWidth();
                temp.setText(lineText.substring(0, matcher.end()));
                temp.applyCss();
                double maxX = temp.getLayoutBounds().getMaxX();
                double startX = maxX - width;

                rectBounds.add(new TextBoundingBox(matcher.group(1),
                        textBounds.getMinX() + startX,
                    textBounds.getMinY() + startY,
                    width, temp.getLayoutBounds().getHeight()));
            }
        }

        return rectBounds;
    }

    /**
     * clear highlights
     */
    public synchronized void clear() {
        List<Rectangle> flatBoxes = new ArrayList<>();
        final Collection<List<Rectangle>> boxesCollection = boxes.values();
        for (List<Rectangle> box : boxesCollection) {
            flatBoxes.addAll(box);
        }
        boxes.clear();
        if(parent!=null) JFXUtilities.runInFXAndWait(()-> getParentChildren(parent).removeAll(flatBoxes));
    }

    public static class TextBoundingBox extends BoundingBox{
        @Getter private final String text;

        public TextBoundingBox(String text, double minX, double minY, double width, double height) {
            super(minX, minY, width, height);
            this.text = text;
        }
    }

}

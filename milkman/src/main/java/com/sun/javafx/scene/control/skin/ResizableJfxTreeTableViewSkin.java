package com.sun.javafx.scene.control.skin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import com.jfoenix.skins.JFXTreeTableViewSkin;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.util.Callback;
import lombok.SneakyThrows;

public class ResizableJfxTreeTableViewSkin<S> extends JFXTreeTableViewSkin<S> {

	private TreeTableView<S> treeTableView;

	public ResizableJfxTreeTableViewSkin(TreeTableView<S> treeTableView) {
		super(treeTableView);
		this.treeTableView = treeTableView;
	}

	public void resizeAllColumns() {
		for (TreeTableColumn<S, ?> column : treeTableView.getColumns()) {
			resizeColumnToFitContent(column, -1);
		}
	}

	@Override
	protected void resizeColumnToFitContent(TreeTableColumn<S, ?> tc, int maxRows) {
		final TreeTableColumn col = tc;
		List<?> items = itemsProperty().get();
		if (items == null || items.isEmpty())
			return;

		Callback cellFactory = col.getCellFactory();
		if (cellFactory == null)
			return;

		TreeTableCell<S, ?> cell = (TreeTableCell) cellFactory.call(col);
		if (cell == null)
			return;

		// set this property to tell the TableCell we want to know its actual
		// preferred width, not the width of the associated TableColumnBase
		cell.getProperties().put(TableCellSkin.DEFER_TO_PARENT_PREF_WIDTH, Boolean.TRUE);

		// determine cell padding
		double padding = 10;
		Node n = cell.getSkin() == null ? null : cell.getSkin().getNode();
		if (n instanceof Region) {
			Region r = (Region) n;
			padding = r.snappedLeftInset() + r.snappedRightInset();
		}

		TreeTableRow<S> treeTableRow = new TreeTableRow<>();
		treeTableRow.updateTreeTableView(treeTableView);

		int rows = maxRows == -1 ? items.size() : Math.min(items.size(), maxRows);
		double maxWidth = 0;
		for (int row = 0; row < rows; row++) {
			treeTableRow.updateIndex(row);
			treeTableRow.updateTreeItem(treeTableView.getTreeItem(row));

			cell.updateTreeTableColumn(col);
			cell.updateTreeTableView(treeTableView);
			cell.updateTreeTableRow(treeTableRow);
			cell.updateIndex(row);

			if ((cell.getText() != null && !cell.getText().isEmpty()) || cell.getGraphic() != null) {
				getChildren().add(cell);
				cell.applyCss();

				double w = cell.prefWidth(-1);

				maxWidth = Math.max(maxWidth, w);
				getChildren().remove(cell);
			}
		}

		// dispose of the cell to prevent it retaining listeners (see RT-31015)
		cell.updateIndex(-1);

		// RT-36855 - take into account the column header text / graphic widths.
		// Magic 10 is to allow for sort arrow to appear without text truncation.
		TableColumnHeader header = getTableHeaderRow().getColumnHeaderFor(tc);
		if (header == null)
			header = getTableHeaderRow().getRootHeader();

		double headerTextWidth = computeTextWidth(getLabel(tc).getFont(), tc.getText(), -1);
		Node graphic = getLabel(tc).getGraphic();
		double headerGraphicWidth = graphic == null ? 0 : graphic.prefWidth(-1) + getLabel(tc).getGraphicTextGap();
		double headerWidth = headerTextWidth + headerGraphicWidth + 10 + header.snappedLeftInset()
				+ header.snappedRightInset();
		maxWidth = Math.max(maxWidth, headerWidth);

		// RT-23486
		maxWidth += padding;
		if (treeTableView.getColumnResizePolicy() == TreeTableView.CONSTRAINED_RESIZE_POLICY) {
			maxWidth = Math.max(maxWidth, col.getWidth());
		}

		col.impl_setWidth(maxWidth);
	}

	@SuppressWarnings("restriction")
	@SneakyThrows
	private double computeTextWidth(Font font, String text, int i) {
		Method method = Utils.class.getDeclaredMethod("computeTextWidth", Font.class, String.class, double.class);
		method.setAccessible(true);
		
		return (double) method.invoke(null, font, text, i);
	}

	@SneakyThrows
	private Label getLabel(TreeTableColumn<S, ?> column) {
//		TableColumnBase column = header.getTableColumn();
		Label label = new Label();
		label.setText(column.getText());
		label.setGraphic(column.getGraphic());
		label.setVisible(column.isVisible());
		return label;
//		   Field field = TableColumnHeader.class.getField("label");
//		   field.setAccessible(true);
//		   return (Label) field.get(header);
	}
}

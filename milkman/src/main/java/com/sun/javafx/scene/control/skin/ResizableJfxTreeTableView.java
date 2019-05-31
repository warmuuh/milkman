package com.sun.javafx.scene.control.skin;

import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import javafx.scene.control.Skin;

public class ResizableJfxTreeTableView<R extends RecursiveTreeObject<R>> extends JFXTreeTableView<R> {

	public ResizableJfxTreeTableView() {
		setSkin(createDefaultSkin());
	}
	@Override
	protected Skin<?> createDefaultSkin() {
		return new ResizableJfxTreeTableViewSkin<>(this);
	}

	public void resizeColumns() {
		
		((ResizableJfxTreeTableViewSkin<R>)getSkin()).resizeAllColumns();
	}
	
}

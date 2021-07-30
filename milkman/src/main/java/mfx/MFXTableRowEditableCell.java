/*
 * Copyright (C) 2021 Parisi Alessandro
 * This file is part of MaterialFX (https://github.com/palexdev/MaterialFX).
 *
 * MaterialFX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MaterialFX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with MaterialFX.  If not, see <http://www.gnu.org/licenses/>.
 */

package mfx;

import io.github.palexdev.materialfx.controls.MFXTableRow;
import io.github.palexdev.materialfx.controls.MFXTableView;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import io.github.palexdev.materialfx.skins.MFXTableRowCellSkin;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Skin;
import milkman.utils.fxml.GenericBinding;

/**
 * This is the implementation of the row cells used by {@link MFXTableView} to fill a {@link MFXTableRow}.
 * <p>
 * Extends {@code Control} so that anyone can implement their own skin if needed.
 * <p>
 * The default skin, {@link MFXTableRowCellSkin}, also allows to place up to two nodes in the cell. These nodes are specified by
 * the following properties, {@link #leadingGraphicProperty()}, {@link #trailingGraphicProperty()}.
 * <p>
 * A little side note, also to respond to some Github issues. It is not recommended to use big nodes. It is not recommended to
 * use too many nodes, that's why it's limited to two. If you need a lot of controls then consider having specific columns which build cells only with graphic
 * like here <a href="https://bit.ly/2SzjrVu">Example</a>.
 * <p>
 * Since it now extends {@code Control} you can easily define your own skin and do whatever you like with the
 * control, just keep in mind that tables are designed to mostly show text.
 * <p>
 * Has two constructors, one with a String and one with a {@link StringExpression}. The first one simply sets the cell's text to the given string,
 * the other one binds the cell's text property to the given string expression.
 * <p>
 * That allows to use {@link MFXTableView} with models which don't use JavaFX's properties. Of course the data won't change automatically in that case,
 * so the table must be updated manually after the data has changed, {@link MFXTableView#updateTable()}.
 */
public class MFXTableRowEditableCell<T> extends MFXTableRowCell {
    //================================================================================
    // Properties
    //================================================================================
    private final StringProperty text = new SimpleStringProperty();
    //for garbage collection:
    private final GenericBinding<T, String> binding;

    public MFXTableRowEditableCell(GenericBinding<T, String> binding) {
        super(new SimpleStringProperty());
        this.binding = binding;
        text.bindBidirectional(binding);
    }



    @Override
    public String getText() {
        return text.get();
    }

    /**
     * Specifies the cell's text. Can also be empty to show only the graphic.
     */
    @Override
    public StringProperty textProperty() {
        return text;
    }

    @Override
    public void setText(String text) {
        this.text.set(text);
    }


    //================================================================================
    // Override Methods
    //================================================================================
    @Override
    protected Skin<?> createDefaultSkin() {
        return new MFXTableRowEditableCellSkin(this);
    }
}

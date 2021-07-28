package milkman.utils.fxml.facade;

import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.skins.MFXComboBoxSkin;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.Skin;
import javafx.util.StringConverter;
import milkman.utils.fxml.GenericBinding;

import java.util.Objects;

public class BindableComboBox<T> extends MFXComboBox<T> {


    private StringConverter<T> converter = new StringConverter<T>() {
        @Override
        public String toString(T object) {
            return Objects.toString(object);
        }

        @Override
        public T fromString(String string) {
            return null;
        }
    };

    public <O> void bindTo(GenericBinding<O, T> binding, O object) {
        binding.bindToUni(getSelectionModel().selectedItemProperty(), object);
        getSelectionModel().selectItem(binding.get());
    }

    public ReadOnlyObjectProperty<T> valueProperty() {
        return getSelectionModel().selectedItemProperty();
    }

    public void setValue(T value) {
        getSelectionModel().selectItem(value);
    }

    public T getValue() {
        return getSelectionModel().getSelectedItem();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new MFXComboBoxSkin<>(this) {
            @Override
            protected String getValueFromItem(T item) {
                return converter.toString(item);
            }
        };
    }

    public void setConverter(StringConverter<T> converter) {
        this.converter = converter;
    }
}

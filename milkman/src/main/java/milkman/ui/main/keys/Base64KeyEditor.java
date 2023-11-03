package milkman.ui.main.keys;

import static milkman.utils.fxml.FxmlBuilder.label;
import static milkman.utils.fxml.FxmlBuilder.text;

import java.util.UUID;
import javafx.scene.Node;
import milkman.domain.KeySet.KeyEntry;
import milkman.ui.plugin.KeyEditor;
import milkman.utils.fxml.FxmlBuilder.VboxExt;
import milkman.utils.fxml.GenericBinding;

public class Base64KeyEditor implements KeyEditor<Base64Key> {

    private final GenericBinding<Base64Key, String> nameBinding = GenericBinding.of(Base64Key::getName, Base64Key::setName);
    private final GenericBinding<Base64Key, String> valueBinding = GenericBinding.of(Base64Key::getValue, Base64Key::setValue);


    @Override
    public String getName() {
        return "Base64 Key";
    }

    @Override
    public Node getRoot(Base64Key keyEntry) {
        var root = new VboxExt();
        root.add(label("Name"));
        var keyName = root.add(text("key-name", "Key Name"));
        root.add(label("Value"));
        var keyValue = root.add(text("key-value", "Key Value"));
        nameBinding.bindTo(keyName.textProperty(), keyEntry);
        valueBinding.bindTo(keyValue.textProperty(), keyEntry);
        return root;
    }

    @Override
    public boolean supportsKeyType(KeyEntry keyEntry) {
        return keyEntry instanceof Base64Key;
    }

    @Override
    public Base64Key getNewKeyEntry() {
        return new Base64Key(UUID.randomUUID().toString(), "", "");
    }
}

package milkman.ui.main.keys;

import javafx.scene.Node;
import milkman.domain.KeySet.KeyEntry;
import milkman.ui.plugin.KeyEditor;
import milkman.utils.fxml.FxmlBuilder.VboxExt;
import milkman.utils.fxml.GenericBinding;

import java.util.UUID;

import static milkman.utils.fxml.FxmlBuilder.label;
import static milkman.utils.fxml.FxmlBuilder.text;

public class PlainKeyEditor implements KeyEditor<PlainKey> {

    private final GenericBinding<PlainKey, String> nameBinding = GenericBinding.of(PlainKey::getName, PlainKey::setName);
    private final GenericBinding<PlainKey, String> valueBinding = GenericBinding.of(PlainKey::getValue, PlainKey::setValue);


    @Override
    public String getName() {
        return "Plain Key";
    }

    @Override
    public Node getRoot(PlainKey keyEntry) {
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
        return keyEntry instanceof PlainKey;
    }

    @Override
    public PlainKey getNewKeyEntry() {
        return new PlainKey(UUID.randomUUID().toString(), "", "");
    }
}

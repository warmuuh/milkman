package milkman.ui.plugin;

import javafx.scene.Node;
import milkman.domain.KeySet.KeyEntry;

public interface KeyEditor<T extends KeyEntry> extends Orderable {

    String getName();



    /**
     * returns the root of the UI-controls for setting up the key editor
     */
    Node getRoot(T keyEntry);

    boolean supportsKeyType(KeyEntry keyEntry);

    T getNewKeyEntry();

}

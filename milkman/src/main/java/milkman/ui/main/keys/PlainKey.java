package milkman.ui.main.keys;

import lombok.Data;
import lombok.NoArgsConstructor;
import milkman.domain.KeySet.KeyEntry;

@Data
@NoArgsConstructor
public class PlainKey extends KeyEntry {
    String value;

    public PlainKey(String id, String name, String value) {
        super(id, name);
        this.value = value;
    }

    @Override
    public String getType() {
        return "Plain";
    }

    @Override
    public String getPreview() {
        return "*".repeat(getValue().length());
    }
}

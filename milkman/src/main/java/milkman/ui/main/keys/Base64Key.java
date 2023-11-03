package milkman.ui.main.keys;

import java.util.Base64;
import lombok.Data;
import lombok.NoArgsConstructor;
import milkman.domain.KeySet.KeyEntry;

@Data
@NoArgsConstructor
public class Base64Key extends KeyEntry {
    String value;

    public Base64Key(String id, String name, String value) {
        super(id, name);
        this.value = value;
    }

    @Override
    public String getType() {
        return "Base64";
    }

    @Override
    public String getPreview() {
        return getValue();
    }

    public String getValue() {
        return Base64.getEncoder().encodeToString(value.getBytes());
    }
}

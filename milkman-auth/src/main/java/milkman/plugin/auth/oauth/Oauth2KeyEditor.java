package milkman.plugin.auth.oauth;

import javafx.scene.Node;
import milkman.domain.KeySet.KeyEntry;
import milkman.ui.plugin.KeyEditor;
import milkman.utils.fxml.FxmlBuilder.VboxExt;
import milkman.utils.fxml.GenericBinding;

import java.util.UUID;

import static milkman.utils.fxml.FxmlBuilder.formEntry;

public class Oauth2KeyEditor implements KeyEditor<Oauth2Credentials> {

    private final GenericBinding<Oauth2Credentials, String> nameBinding = GenericBinding.of(Oauth2Credentials::getName, Oauth2Credentials::setName);
    private final GenericBinding<Oauth2Credentials, String> endpointBinding = GenericBinding.of(Oauth2Credentials::getAccessTokenEndpoint, Oauth2Credentials::setAccessTokenEndpoint);
    private final GenericBinding<Oauth2Credentials, String> clientIdBinding = GenericBinding.of(Oauth2Credentials::getClientId, Oauth2Credentials::setClientId);
    private final GenericBinding<Oauth2Credentials, String> clientSecretBinding = GenericBinding.of(Oauth2Credentials::getClientSecret, Oauth2Credentials::setClientSecret);
    private final GenericBinding<Oauth2Credentials, String> scopesBinding = GenericBinding.of(Oauth2Credentials::getScopes, Oauth2Credentials::setScopes);


    @Override
    public String getName() {
        return "Oauth2 Credentials";
    }

    @Override
    public Node getRoot(Oauth2Credentials keyEntry) {
        var root = new VboxExt();
        root.add(formEntry("Name", nameBinding, keyEntry));
        root.add(formEntry("Token Endpoint", endpointBinding, keyEntry));
        root.add(formEntry("Client Id", clientIdBinding, keyEntry));
        root.add(formEntry("Client Secret", clientSecretBinding, keyEntry));
        root.add(formEntry("Scopes", scopesBinding, keyEntry));
        return root;
    }

    @Override
    public boolean supportsKeyType(KeyEntry keyEntry) {
        return keyEntry instanceof Oauth2Credentials;
    }

    @Override
    public Oauth2Credentials getNewKeyEntry() {
        return new Oauth2Credentials(UUID.randomUUID().toString(), "");
    }
}

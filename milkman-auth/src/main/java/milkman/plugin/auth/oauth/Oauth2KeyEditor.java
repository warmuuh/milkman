package milkman.plugin.auth.oauth;

import com.jfoenix.controls.JFXComboBox;
import javafx.application.Platform;
import javafx.scene.Node;
import milkman.domain.KeySet.KeyEntry;
import milkman.plugin.auth.oauth.model.Oauth2Credentials;
import milkman.plugin.auth.oauth.model.Oauth2Grant.ClientCredentialGrant;
import milkman.plugin.auth.oauth.model.Oauth2Grant.PasswordGrant;
import milkman.ui.plugin.KeyEditor;
import milkman.utils.fxml.FxmlBuilder.*;
import milkman.utils.fxml.GenericBinding;

import java.util.UUID;

import static milkman.utils.fxml.FxmlBuilder.*;

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

        root.add(label("Grant Type"));
        var combobox = root.add(new JFXComboBox<GrantTypeBuilder>());
        combobox.getItems().addAll(
                new GrantTypeBuilder.ClientCredentialBuilder(),
                new GrantTypeBuilder.PasswordBuilder());

        var grantArea = root.add(vbox());

        combobox.valueProperty().addListener(((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                grantArea.getChildren().clear();
                grantArea.add(newValue.getEditor(keyEntry));
            });
        }));

        if (keyEntry.getGrantType() == null || keyEntry.getGrantType() instanceof ClientCredentialGrant) {
            combobox.setValue(combobox.getItems().get(0));
        } else if (keyEntry.getGrantType() instanceof PasswordGrant){
            combobox.setValue(combobox.getItems().get(1));
        }

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

    abstract static class GrantTypeBuilder {
        abstract Node getEditor(Oauth2Credentials keyEntry);
        abstract String getName();
        public String toString() {
            return getName();
        }


        static class ClientCredentialBuilder extends GrantTypeBuilder{

            @Override
            Node getEditor(Oauth2Credentials keyEntry) {
                var grantType = keyEntry.getGrantType() instanceof ClientCredentialGrant ? (ClientCredentialGrant)keyEntry.getGrantType() : new ClientCredentialGrant();
                keyEntry.setGrantType(grantType);

                return hbox();
            }

            @Override
            String getName() {
                return "Client Credentials Grant";
            }
        }


        static class PasswordBuilder extends GrantTypeBuilder{
            private final GenericBinding<PasswordGrant, String> usernameBinding = GenericBinding.of(PasswordGrant::getUsername, PasswordGrant::setUsername);
            private final GenericBinding<PasswordGrant, String> passwordBinding = GenericBinding.of(PasswordGrant::getPassword, PasswordGrant::setPassword);

            @Override
            Node getEditor(Oauth2Credentials keyEntry) {
                var grantType = keyEntry.getGrantType() instanceof PasswordGrant ? (PasswordGrant)keyEntry.getGrantType() : new PasswordGrant();
                keyEntry.setGrantType(grantType);
                var root = vbox();
                root.add(formEntry("Username", usernameBinding, grantType));
                root.add(formEntry("Password", passwordBinding, grantType));
                return root;
            }

            @Override
            String getName() {
                return "Password Grant";
            }
        }

    }

}

package milkman.plugin.auth.oauth;

import javafx.scene.Node;
import milkman.plugin.auth.oauth.model.Oauth2Credentials;
import milkman.plugin.auth.oauth.model.Oauth2Grant;
import milkman.plugin.auth.oauth.model.Oauth2Grant.AuthorizationCodeGrant;
import milkman.plugin.auth.oauth.model.Oauth2Grant.ClientCredentialGrant;
import milkman.plugin.auth.oauth.model.Oauth2Grant.PasswordGrant;
import milkman.utils.fxml.GenericBinding;

import java.lang.reflect.ParameterizedType;

import static milkman.utils.fxml.FxmlBuilder.*;

abstract class GrantTypeBuilder<T extends Oauth2Grant> {
    Node getEditor(Oauth2Credentials keyEntry) {
        var supportedGrantType = supportedGrantType();
        try {
            T grantType = supportedGrantType.isInstance(keyEntry.getGrantType()) ? (T) keyEntry.getGrantType() : supportedGrantType.newInstance();
            keyEntry.setGrantType(grantType);
            return getEditor(grantType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate grant type", e);
        }
    }

    abstract Node getEditor(T grant);

    abstract String getName();

    public String toString() {
        return getName();
    }

    Class<T> supportedGrantType() {
        return (Class<T>)
                ((ParameterizedType) getClass()
                        .getGenericSuperclass())
                        .getActualTypeArguments()[0];
    }

    static class ClientCredentialBuilder extends GrantTypeBuilder<ClientCredentialGrant> {
        @Override
        Node getEditor(ClientCredentialGrant grant) {
            return hbox();
        }

        @Override
        String getName() {
            return "Client Credentials Grant";
        }
    }


    static class PasswordBuilder extends GrantTypeBuilder<PasswordGrant> {
        private final GenericBinding<PasswordGrant, String> usernameBinding = GenericBinding.of(PasswordGrant::getUsername, PasswordGrant::setUsername);
        private final GenericBinding<PasswordGrant, String> passwordBinding = GenericBinding.of(PasswordGrant::getPassword, PasswordGrant::setPassword);

        @Override
        Node getEditor(PasswordGrant grant) {
            var root = vbox();
            root.setSpacing(25);
            root.add(vbox()); //small spacer to top
            root.add(formEntry("Username", usernameBinding, grant));
            root.add(formEntry("Password", passwordBinding, grant));
            return root;
        }

        @Override
        String getName() {
            return "Password Grant";
        }
    }

    static class AuthorizationCodeBuilder extends GrantTypeBuilder<AuthorizationCodeGrant> {
        private final GenericBinding<AuthorizationCodeGrant, String> authEndpointBinding = GenericBinding.of(AuthorizationCodeGrant::getAuthorizationEndpoint, AuthorizationCodeGrant::setAuthorizationEndpoint);

        @Override
        Node getEditor(AuthorizationCodeGrant grant) {
            var root = vbox();
            root.setSpacing(25);
            root.add(vbox()); //small spacer to top
            root.add(formEntry("Authorization Endpoint", authEndpointBinding, grant));
            return root;
        }

        @Override
        String getName() {
            return "Authorization Code Grant";
        }
    }
}

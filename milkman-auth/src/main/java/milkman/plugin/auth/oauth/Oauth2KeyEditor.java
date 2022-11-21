package milkman.plugin.auth.oauth;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import milkman.domain.KeySet.KeyEntry;
import milkman.plugin.auth.oauth.GrantTypeBuilder.ClientCredentialBuilder;
import milkman.plugin.auth.oauth.GrantTypeBuilder.PasswordBuilder;
import milkman.plugin.auth.oauth.model.OAuth2Token;
import milkman.plugin.auth.oauth.model.Oauth2Credentials;
import milkman.ui.main.Toaster;
import milkman.ui.plugin.KeyEditor;
import milkman.ui.plugin.ToasterAware;
import milkman.utils.fxml.FxmlBuilder.*;
import milkman.utils.fxml.GenericBinding;

import java.util.UUID;

import static milkman.plugin.auth.oauth.GrantTypeBuilder.AuthorizationCodeBuilder;
import static milkman.utils.fxml.FxmlBuilder.*;

public class Oauth2KeyEditor implements KeyEditor<Oauth2Credentials>, ToasterAware {

    private final GenericBinding<Oauth2Credentials, String> nameBinding = GenericBinding.of(Oauth2Credentials::getName, Oauth2Credentials::setName);
    private final GenericBinding<Oauth2Credentials, String> endpointBinding = GenericBinding.of(Oauth2Credentials::getAccessTokenEndpoint, Oauth2Credentials::setAccessTokenEndpoint);
    private final GenericBinding<Oauth2Credentials, String> clientIdBinding = GenericBinding.of(Oauth2Credentials::getClientId, Oauth2Credentials::setClientId);
    private final GenericBinding<Oauth2Credentials, String> clientSecretBinding = GenericBinding.of(Oauth2Credentials::getClientSecret, Oauth2Credentials::setClientSecret);
    private final GenericBinding<Oauth2Credentials, String> scopesBinding = GenericBinding.of(Oauth2Credentials::getScopes, Oauth2Credentials::setScopes);
    private final GenericBinding<Oauth2Credentials, Boolean> autoRefreshBinding = GenericBinding.of(Oauth2Credentials::isAutoRefresh, Oauth2Credentials::setAutoRefresh);
    private final GenericBinding<Oauth2Credentials, Boolean> autoIssueBinding = GenericBinding.of(Oauth2Credentials::isAutoIssue, Oauth2Credentials::setAutoIssue);
    private final GenericBinding<Oauth2Credentials, Boolean> requestBodyAuthSchemeBinding = GenericBinding.of(Oauth2Credentials::isRequestBodyAuthScheme, Oauth2Credentials::setRequestBodyAuthScheme);

    private Toaster toaster;
    private JFXTextField txtRefreshToken;
    private JFXTextField txtExpires;
    private JFXTextField txtAccessToken;


    @Override
    public String getName() {
        return "Oauth2 Credentials";
    }

    @Override
    public Node getRoot(Oauth2Credentials keyEntry) {
        var root = new VboxExt();
        root.setSpacing(25);
        root.setMinWidth(600);

        root.add(formEntry("Name", nameBinding, keyEntry));
        root.add(formEntry("Token Endpoint", endpointBinding, keyEntry));
        root.add(formEntry("Client Id", clientIdBinding, keyEntry));
        root.add(formEntry("Client Secret", clientSecretBinding, keyEntry));
        root.add(formEntry("Scopes", scopesBinding, keyEntry));

        var autoRefresh = new JFXToggleButton();
        autoRefresh.setText("Refresh Token on expiry");
        autoRefreshBinding.bindTo(autoRefresh.selectedProperty(), keyEntry);

        var autoIssue = new JFXToggleButton();
        autoIssue.setText("Issue new token if necessary");
        autoIssueBinding.bindTo(autoIssue.selectedProperty(), keyEntry);


        var requestBodyAuthScheme = new JFXToggleButton();
        requestBodyAuthScheme.setText("Credentials in Body");
        requestBodyAuthSchemeBinding.bindTo(requestBodyAuthScheme.selectedProperty(), keyEntry);

        root.add(new HBox(autoRefresh, autoIssue, requestBodyAuthScheme));

        var combobox = root.add(new JFXComboBox<GrantTypeBuilder>());
        combobox.getItems().addAll(
                new ClientCredentialBuilder(),
                new PasswordBuilder(),
                new AuthorizationCodeBuilder());

        var grantArea = root.add(vbox());

        root.add(vbox(label("Grant Type"), combobox, grantArea));

        combobox.valueProperty().addListener(((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                grantArea.getChildren().clear();
                grantArea.add(newValue.getEditor(keyEntry));
            });
        }));

        var activeBuilder = combobox.getItems().stream()
                .filter(b -> b.supportedGrantType().isInstance(keyEntry.getGrantType()))
                .findAny()
                .orElse(combobox.getItems().get(0));
        combobox.setValue(activeBuilder);

        var btnFetchToken = button("Fetch Token", () -> fetchToken(keyEntry));
        btnFetchToken.getStyleClass().add("primary-button");

        var btnRefreshToken = button("Refresh Token", () -> refreshToken(keyEntry));
        btnRefreshToken.getStyleClass().add("primary-button");

        var hbox = root.add(hbox(btnFetchToken, btnRefreshToken));
        hbox.setSpacing(5);

        this.txtAccessToken = root.add(text("accessToken", "Access Token", true));
        txtAccessToken.setEditable(false);

        this.txtExpires = root.add(text("expires-at", "Expires", true));
        txtExpires.setEditable(false);

        this.txtRefreshToken = root.add(text("refreshToken", "Refresh Token", true));
        txtRefreshToken.setEditable(false);

        showTokenDetails(keyEntry.getToken());

        return root;
    }

    private void fetchToken(Oauth2Credentials keyEntry) {
        if (keyEntry.getGrantType() == null) {
            toaster.showToast("No Granttype chosen");
            return;
        }
        try {
            keyEntry.fetchNewToken();
        } catch (Exception e) {
            toaster.showToast(e.getMessage());
        }
        showTokenDetails(keyEntry.getToken());
    }

    private void refreshToken(Oauth2Credentials keyEntry) {
        if (keyEntry.getGrantType() == null) {
            toaster.showToast("No Granttype chosen");
            return;
        }
        try {
            keyEntry.refreshToken();
        } catch (Exception e) {
            toaster.showToast(e.getMessage());
        }
        showTokenDetails(keyEntry.getToken());
    }

    private void showTokenDetails(OAuth2Token token) {
        if (token != null) {
            txtAccessToken.setText(token.getAccessToken());
            txtExpires.setText(token.getExpiresAt() != null ? token.getExpiresAt().toString() : "-");
            txtRefreshToken.setText(token.getRefreshToken() != null ? token.getRefreshToken() : "-");
        } else {
            txtAccessToken.setText("-");
            txtExpires.setText("-");
            txtRefreshToken.setText("-");
        }
    }

    @Override
    public boolean supportsKeyType(KeyEntry keyEntry) {
        return keyEntry instanceof Oauth2Credentials;
    }

    @Override
    public Oauth2Credentials getNewKeyEntry() {
        return new Oauth2Credentials(UUID.randomUUID().toString(), "");
    }

    @Override
    public void setToaster(Toaster toaster) {
        this.toaster = toaster;
    }

}

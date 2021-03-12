package milkman.plugin.auth.oauth.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import milkman.plugin.auth.oauth.DynamicOauth2Api;
import milkman.plugin.auth.oauth.server.AuthorizationCodeCaptureServer;
import milkman.ui.main.dialogs.WaitForMonoDialog;

import java.awt.*;
import java.net.URI;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(include = As.PROPERTY, use = Id.CLASS, visible = true)
public interface Oauth2Grant {

	OAuth2AccessToken getToken(String clientId, String clientSecret, String accessTokenEndpoint, String scopes) throws Exception;

	@Data
	@NoArgsConstructor
	class ClientCredentialGrant implements Oauth2Grant {
		@Override
		public OAuth2AccessToken getToken(String clientId, String clientSecret, String accessTokenEndpoint, String scopes) throws Exception {
			OAuth20Service service = new ServiceBuilder(clientId)
					.apiSecret(clientSecret)
					.build(new DynamicOauth2Api(accessTokenEndpoint, ""));
			return service.getAccessTokenClientCredentialsGrant(scopes);
		}
	}

	@Data
	@NoArgsConstructor
	class PasswordGrant implements Oauth2Grant {
		String username;
		String password;
		@Override
		public OAuth2AccessToken getToken(String clientId, String clientSecret, String accessTokenEndpoint, String scopes) throws Exception {
			OAuth20Service service = new ServiceBuilder(clientId)
					.apiSecret(clientSecret)
					.build(new DynamicOauth2Api(accessTokenEndpoint, ""));
			return service.getAccessTokenPasswordGrant(username, password, scopes);
		}
	}

	@Data
	@Slf4j
	@NoArgsConstructor
	class AuthorizationCodeGrant implements Oauth2Grant {
		String authorizationEndpoint;

		@Override
		public OAuth2AccessToken getToken(String clientId, String clientSecret, String accessTokenEndpoint, String scopes) throws Exception {
			AuthorizationCodeCaptureServer server = new AuthorizationCodeCaptureServer();

			OAuth20Service service = new ServiceBuilder(clientId)
					.apiSecret(clientSecret)
					.callback(server.getReturnUrl())
					.build(new DynamicOauth2Api(accessTokenEndpoint, authorizationEndpoint));

			var authorizationUrl = service.createAuthorizationUrlBuilder()
					.scope(scopes)
					.build();

			log.info("Redirecting to " + authorizationUrl);
			Desktop.getDesktop().browse(new URI(authorizationUrl));
			var dialog = new WaitForMonoDialog<String>();
			dialog.showAndWait("Waiting for Authorization Code ...", server.listenForCode());
			if (dialog.isCancelled()){
				throw new IllegalStateException("Authorization Flow got cancelled");
			}
			var code = dialog.getValue();
			return service.getAccessToken(code);
		}
	}
}

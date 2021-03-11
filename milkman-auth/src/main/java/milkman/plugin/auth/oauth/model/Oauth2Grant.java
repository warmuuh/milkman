package milkman.plugin.auth.oauth.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.microsoft.alm.oauth2.useragent.JavaFx;
import com.microsoft.alm.oauth2.useragent.UserAgent;
import com.microsoft.alm.oauth2.useragent.UserAgentImpl;
import lombok.Data;
import lombok.NoArgsConstructor;
import milkman.plugin.auth.oauth.DynamicOauth2Api;

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
	@NoArgsConstructor
	class AuthorizationCodeGrant implements Oauth2Grant {
		String authorizationEndpoint;
		String redirectUrl;
		@Override
		public OAuth2AccessToken getToken(String clientId, String clientSecret, String accessTokenEndpoint, String scopes) throws Exception {
			OAuth20Service service = new ServiceBuilder(clientId)
					.apiSecret(clientSecret)
					.build(new DynamicOauth2Api(accessTokenEndpoint, authorizationEndpoint));

			var authorizationUrl = service.createAuthorizationUrlBuilder().scope(scopes).build();

			UserAgent userAgent = new UserAgentImpl();
			JavaFx.
			AuthorizationResponse authorizationResponse = userAgent.requestAuthorizationCode(URI.create(authorizationUrl), URI.create(redirectUrl));
			String code = authorizationResponse.getCode();

			return service.getAccessToken(code);
		}
	}
}

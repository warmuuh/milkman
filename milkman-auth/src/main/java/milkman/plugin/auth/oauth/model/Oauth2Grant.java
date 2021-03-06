package milkman.plugin.auth.oauth.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, visible = true)
public interface Oauth2Grant {

	OAuth2AccessToken getToken(OAuth20Service service, String scopes) throws Exception;

	@Data
	@NoArgsConstructor
	class ClientCredentialGrant implements Oauth2Grant {
		public OAuth2AccessToken getToken(OAuth20Service service, String scopes) throws Exception {
			return service.getAccessTokenClientCredentialsGrant(scopes);
		}
	}

	@Data
	@NoArgsConstructor
	class PasswordGrant implements Oauth2Grant {
		String username;
		String password;
		public OAuth2AccessToken getToken(OAuth20Service service, String scopes) throws Exception {
			return service.getAccessTokenPasswordGrant(username, password, scopes);
		}
	}
}

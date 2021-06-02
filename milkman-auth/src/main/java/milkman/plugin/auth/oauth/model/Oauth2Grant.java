package milkman.plugin.auth.oauth.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(include = As.PROPERTY, use = Id.CLASS, visible = true)
public interface Oauth2Grant {

	OAuth2Token getToken(Oauth2Api api, String scopes, boolean useReqBodyAuthScheme);

	@Data
	@NoArgsConstructor
	class ClientCredentialGrant implements Oauth2Grant {
		@Override
		public OAuth2Token getToken(Oauth2Api api, String scopes, boolean useReqBodyAuthScheme){
			return api.clientCredentialGrant(scopes, useReqBodyAuthScheme);
		}
	}

	@Data
	@NoArgsConstructor
	class PasswordGrant implements Oauth2Grant {
		String username;
		String password;
		@Override
		public OAuth2Token getToken(Oauth2Api api, String scopes, boolean useReqBodyAuthScheme){
			return api.passwordGrant(username, password, scopes, useReqBodyAuthScheme);
		}
	}

	@Data
	@Slf4j
	@NoArgsConstructor
	class AuthorizationCodeGrant implements Oauth2Grant {
		String authorizationEndpoint;

		@Override
		public OAuth2Token getToken(Oauth2Api api, String scopes, boolean useReqBodyAuthScheme){
			return api.authenticationCodeGrant(authorizationEndpoint, scopes, useReqBodyAuthScheme);
		}
	}
}

package milkman.plugin.auth.oauth.model;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.extractors.OAuth2AccessTokenJsonExtractor;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClientConfig;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth2.clientauthentication.ClientAuthentication;
import com.github.scribejava.core.oauth2.clientauthentication.HttpBasicAuthenticationScheme;
import com.github.scribejava.core.oauth2.clientauthentication.RequestBodyAuthenticationScheme;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import milkman.PlatformUtil;
import milkman.plugin.auth.oauth.DynamicOauth2Api;
import milkman.plugin.auth.oauth.scribe.JDKHttpClient;
import milkman.plugin.auth.oauth.server.AuthorizationCodeCaptureServer;
import milkman.ui.main.dialogs.WaitForMonoDialog;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@RequiredArgsConstructor
public class Oauth2Api {
	private interface ScribeTokenSupplier {
		OAuth2AccessToken get() throws IOException, InterruptedException, ExecutionException, URISyntaxException;
	}

	private final String clientId;
	private final String clientSecret;
	private final String accessTokenEndpoint;

	public OAuth2Token refreshToken(OAuth2Token oldToken, boolean useReqBodyAuthScheme) {
		OAuth20Service service = getOauthService(useReqBodyAuthScheme);
		return getToken(() -> service.refreshAccessToken(oldToken.getRefreshToken()));
	}

	public OAuth2Token clientCredentialGrant(String scopes, boolean useReqBodyAuthScheme, Map<String, String> extraParams) {
		DynamicOauth2Api api = getApi(useReqBodyAuthScheme);
		OAuth20Service service = getOauthService(useReqBodyAuthScheme);
		return getToken(() -> {
			OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
			api.getClientAuthentication().addClientAuthentication(request, clientId, clientSecret);
			if (scopes != null) {
				request.addParameter(OAuthConstants.SCOPE, scopes);
			}
			request.addParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.CLIENT_CREDENTIALS);
			extraParams.forEach(request::addParameter);
			var response = service.execute(request);
			return OAuth2AccessTokenJsonExtractor.instance().extract(response);
		});
	}


	public OAuth2Token passwordGrant(String username, String password, String scopes, boolean useReqBodyAuthScheme, Map<String, String> extraParams) {
		DynamicOauth2Api api = getApi(useReqBodyAuthScheme);
		OAuth20Service service = getOauthService(useReqBodyAuthScheme);
		return getToken(() -> {
			OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
			api.getClientAuthentication().addClientAuthentication(request, clientId, clientSecret);
			request.addParameter(OAuthConstants.USERNAME, username);
			request.addParameter(OAuthConstants.PASSWORD, password);
			if (scopes != null) {
				request.addParameter(OAuthConstants.SCOPE, scopes);
			}
			request.addParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.PASSWORD);
			extraParams.forEach(request::addParameter);
			var response = service.execute(request);
			return OAuth2AccessTokenJsonExtractor.instance().extract(response);
		});
	}


	public OAuth2Token authenticationCodeGrant(String authorizationEndpoint, String scopes, boolean useReqBodyAuthScheme, Map<String, String> extraParams) {
			return getToken(() -> {
				AuthorizationCodeCaptureServer server = new AuthorizationCodeCaptureServer();
				DynamicOauth2Api api = getApi(server.getReturnUrl(), authorizationEndpoint, useReqBodyAuthScheme);
				OAuth20Service service = getOauthService(server.getReturnUrl(), authorizationEndpoint, useReqBodyAuthScheme);

				var authorizationUrl = service.createAuthorizationUrlBuilder()
						.scope(scopes)
						.build();

				log.info("Redirecting to " + authorizationUrl);
				if (!PlatformUtil.tryOpenBrowser(authorizationUrl)){
					throw new RuntimeException("Failed to open browser");
				}
				var dialog = new WaitForMonoDialog<String>();
				dialog.showAndWait("Waiting for Authorization Code ...", server.listenForCode());
				if (dialog.isCancelled()){
					throw new IllegalStateException("Authorization Flow got cancelled");
				}
				var code = dialog.getValue();
				OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
				api.getClientAuthentication().addClientAuthentication(request, clientId, clientSecret);
				request.addParameter(OAuthConstants.CODE, code);
				request.addParameter(OAuthConstants.REDIRECT_URI, server.getReturnUrl());
				request.addParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.AUTHORIZATION_CODE);
				extraParams.forEach(request::addParameter);
				var response = service.execute(request);
				return OAuth2AccessTokenJsonExtractor.instance().extract(response);
			});
	}

	private OAuth2Token getToken(ScribeTokenSupplier tokenSupplier) {
		try {
			var scribeToken = tokenSupplier.get();
			var expiryDate = (Date)null;
			if (scribeToken.getExpiresIn() != null) {
				expiryDate = new Date(Instant.now().plusSeconds(scribeToken.getExpiresIn()).toEpochMilli());
			}
			return new OAuth2Token(scribeToken.getAccessToken(), scribeToken.getRefreshToken(), expiryDate);
		} catch (OAuth2AccessTokenErrorResponse e){
			String msg = e.getErrorDescription() != null ? e.getErrorDescription() : e.getMessage();
			throw new RuntimeException(msg, e);
		} catch (Exception e) {
			log.error("Failed to fetch token", e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private DynamicOauth2Api getApi(boolean useReqBodyAuthScheme) {
		return getApi(null, "", useReqBodyAuthScheme);
	}

	private DynamicOauth2Api getApi(String callback, String authorizationBaseUrl, boolean useReqBodyAuthScheme) {
		ClientAuthentication clientAuthentication = useReqBodyAuthScheme
				? RequestBodyAuthenticationScheme.instance()
				: HttpBasicAuthenticationScheme.instance();
		return new DynamicOauth2Api(accessTokenEndpoint, authorizationBaseUrl, clientAuthentication);
	}

	private OAuth20Service getOauthService(boolean useReqBodyAuthScheme) {
		return getOauthService(null, useReqBodyAuthScheme);
	}

	private OAuth20Service getOauthService(String callback, boolean useReqBodyAuthScheme) {
		return getOauthService(callback, "", useReqBodyAuthScheme);
	}

	private OAuth20Service getOauthService(String callback, String authorizationBaseUrl, boolean useReqBodyAuthScheme) {
		return new ServiceBuilder(clientId)
				.apiSecret(clientSecret)
				.callback(callback)
				.httpClient(new JDKHttpClient(JDKHttpClientConfig.defaultConfig().withConnectTimeout(1000).withReadTimeout(2000)))
				.build(getApi(callback, authorizationBaseUrl, useReqBodyAuthScheme));
	}

}

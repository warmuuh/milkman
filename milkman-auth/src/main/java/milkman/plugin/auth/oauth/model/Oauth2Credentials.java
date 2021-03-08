package milkman.plugin.auth.oauth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse;
import com.github.scribejava.core.oauth.OAuth20Service;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.KeySet.KeyEntry;
import milkman.plugin.auth.oauth.DynamicOauth2Api;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.Date;

@Slf4j
@Data
@NoArgsConstructor
public class Oauth2Credentials extends KeyEntry {


    String clientId;
    String clientSecret;
    String accessTokenEndpoint;
    String scopes;
    boolean autoRefresh;

    @JsonIgnore
    boolean refreshFailed;

    Oauth2Grant grantType;
    OAuth2Token token;


    public Oauth2Credentials(String id, String name) {
        super(id, name);
    }

    @Override
    public String getType() {
        if (isExpired()) {
            return "Oauth2 (expired)";
        }

        return "Oauth2";
    }

    @Override
    public String getValue() {
        if (token == null){
            return "<no token>";
        }
        if (isExpired() && isAutoRefresh() && token.getRefreshToken() != null && !refreshFailed){
            try {
                refreshToken();
            } catch (Exception e) {
                log.error("Failed to refresh token", e.getMessage());
                refreshFailed = true;
            }
        }
        return token.getAccessToken();
    }


    private boolean isExpired() {
        return token != null && token.getExpiresAt().before(new Date());
    }

    public void refreshToken() {
        if (!autoRefresh || StringUtils.isBlank(token.getRefreshToken())){
            return;
        }

        OAuth20Service service = new ServiceBuilder(clientId)
                .apiSecret(clientSecret)
                .build(new DynamicOauth2Api(accessTokenEndpoint, ""));
        try {
            var scribeToken = service.refreshAccessToken(token.getRefreshToken());
            token = new OAuth2Token(scribeToken.getAccessToken(), scribeToken.getRefreshToken(), new Date(Instant.now().plusSeconds(scribeToken.getExpiresIn()).toEpochMilli()));
        } catch (OAuth2AccessTokenErrorResponse e){
            throw new RuntimeException(e.getErrorDescription(), e);
        } catch (Exception e) {
            log.error("Failed to fetch token", e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void fetchNewToken() {
        OAuth20Service service = new ServiceBuilder(clientId)
                .apiSecret(clientSecret)
                .build(new DynamicOauth2Api(accessTokenEndpoint, ""));
        try {
            var scibeToken = grantType.getToken(service, scopes);
            token = new OAuth2Token(scibeToken.getAccessToken(), scibeToken.getRefreshToken(), new Date(Instant.now().plusSeconds(scibeToken.getExpiresIn()).toEpochMilli()));
        } catch (OAuth2AccessTokenErrorResponse e){
            throw new RuntimeException(e.getErrorDescription(), e);
        } catch (Exception e) {
            log.error("Failed to fetch token", e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}

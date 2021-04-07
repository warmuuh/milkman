package milkman.plugin.auth.oauth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.KeySet.KeyEntry;
import org.apache.commons.lang3.StringUtils;

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
        // trigger auto-refresh
        getValue();

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
        if (StringUtils.isBlank(token.getRefreshToken())){
            return;
        }

        Oauth2Api api = new Oauth2Api(clientId, clientSecret, accessTokenEndpoint);

        var refreshedToken = api.refreshToken(token);
        token.setAccessToken(refreshedToken.getAccessToken());
        token.setExpiresAt(refreshedToken.getExpiresAt());
        if (StringUtils.isNotBlank(refreshedToken.getRefreshToken())) {
            token.setRefreshToken(refreshedToken.getRefreshToken());
        }
    }

    public void fetchNewToken() {
        Oauth2Api api = new Oauth2Api(clientId, clientSecret, accessTokenEndpoint);
        token = grantType.getToken(api, scopes);
    }

}

package milkman.plugin.auth.oauth.model;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse;
import com.github.scribejava.core.oauth.OAuth20Service;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import milkman.domain.KeySet.KeyEntry;
import milkman.plugin.auth.oauth.DynamicOauth2Api;
import org.apache.commons.lang3.ObjectUtils;

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

    Oauth2Grant grantType;
    OAuth2Token token;


    public Oauth2Credentials(String id, String name) {
        super(id, name);
    }

    @Override
    public String getType() {
        return "Oauth2";
    }

    @Override
    public String getValue() {
        if (token == null && ObjectUtils.allNotNull(clientId, clientSecret, accessTokenEndpoint, scopes)){
            try{
                fetchNewToken();
            } catch (RuntimeException e) {
                return "<failed: "+e.getMessage()+">";
            }
        }
        if (token == null){
            return "<no token>";
        }
        return token.getAccessToken();
    }

    private void fetchNewToken() {
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

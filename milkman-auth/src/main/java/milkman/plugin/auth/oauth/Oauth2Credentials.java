package milkman.plugin.auth.oauth;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import lombok.Data;
import lombok.NoArgsConstructor;
import milkman.domain.KeySet.KeyEntry;
import org.apache.commons.lang3.ObjectUtils;

import java.time.Instant;
import java.util.Date;

@Data
@NoArgsConstructor
public class Oauth2Credentials extends KeyEntry {


    String clientId;
    String clientSecret;
    String accessTokenEndpoint;
    String scopes;

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
            fetchNewToken();
        }
        return token.getAccessToken();
    }

    private void fetchNewToken() {
        OAuth20Service service = new ServiceBuilder(clientId)
                .apiSecret(clientSecret)
                .build(new DynamicOauth2Api(accessTokenEndpoint, ""));
        try {
            var scibeToken = service.getAccessTokenClientCredentialsGrant(scopes);
            token = new OAuth2Token(scibeToken.getAccessToken(), scibeToken.getRefreshToken(), new Date(Instant.now().plusSeconds(scibeToken.getExpiresIn()).toEpochMilli()));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}

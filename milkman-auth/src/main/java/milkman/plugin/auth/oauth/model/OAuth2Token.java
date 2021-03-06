package milkman.plugin.auth.oauth.model;

import lombok.Value;

import java.util.Date;

@Value
public class OAuth2Token {
    String accessToken;
    String refreshToken;
    Date expiresAt;
}

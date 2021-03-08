package milkman.plugin.auth.oauth.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2Token {
    String accessToken;
    String refreshToken;
    Date expiresAt;
}

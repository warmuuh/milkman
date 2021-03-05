package milkman.plugin.auth.oauth;

import com.github.scribejava.core.builder.api.DefaultApi20;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DynamicOauth2Api extends DefaultApi20 {
    private final String accessTokenEndpoint;
    private final String authorizationBaseUrl;
}

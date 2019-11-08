package milkman.ui.plugin.rest;


import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import java.net.PasswordAuthentication;

class HttpUtilTest {

	@Test
	void shouldEncodeCredentialsCorrectly() {
		assertThat(HttpUtil.authorizationHeaderValue(new PasswordAuthentication("aladdin", "opensesame".toCharArray())))
			.isEqualTo("Basic YWxhZGRpbjpvcGVuc2VzYW1l");
	}

}

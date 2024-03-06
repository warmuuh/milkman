package milkman.ui.plugin.rest.curl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CurlImporterTest {


    @Test
    void shouldParseCommand(){
        var args = CurlImporter.translateCommandline("curl -H \"X-you-and-me: yes\" -H 'unix: header' ^\n \\\n www.love.com");
        assertThat(args).containsExactly("curl",
                "-H", "X-you-and-me: yes",
                "-H", "unix: header",
                "www.love.com");
    }

    @Test
    void shouldParseCommandPostCompressed(){
        var args = CurlImporter.translateCommandline("curl --data-binary \"{\\\"somekey\\\":\\\"somevalue\\\"}\" --compressed \"https://api.leboncoin.fr/api/adfinder/v1/around_me\"");
        assertThat(args).containsExactly("curl",
            "--data-binary",
            "{\"somekey\":\"somevalue\"}",
            "--compressed",
            "https://api.leboncoin.fr/api/adfinder/v1/around_me");
    }

}
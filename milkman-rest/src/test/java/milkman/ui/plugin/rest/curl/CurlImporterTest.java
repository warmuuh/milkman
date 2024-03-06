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

    @Test
    void shouldParseCommandPostCompressed2(){
        var args = CurlImporter.translateCommandline("curl -H \"Host: api.leboncoin.fr\" -H \"Cookie: didomi_token=eyJ1c2VyX2lkIjoiOUU3NEVEMDktRTI4Ny00QTZFLTg2NjAtQkFCRjg2N0RFQjlDIiwicHVycG9zZXMiOnsiZW5hYmxlZCI6WyJwcml4IiwiaW1wcm92ZV9wcm9kdWN0cyIsImdlb2xvY2F0aW9uX2RhdGEiLCJuZWNlc3NhaXJlcyIsIm1lc3VyZWF1ZGllbmNlIiwibWFya2V0X3Jlc2VhcmNoIiwicGVyc29ubmFsaXNhdGlvbm1hcmtldGluZyIsInNlbGVjdF9iYXNpY19hZHMiLCJzZWxlY3RfcGVyc29uYWxpemVkX2FkcyIsImV4cGVyaWVuY2V1dGlsaXNhdGV1ciIsIm1lYXN1cmVfYWRfcGVyZm9ybWFuY2UiLCJkZXZpY2VfY2hhcmFjdGVyaXN0aWNzIiwiY29va2llcyIsImNyZWF0ZV9hZHNfcHJvZmlsZSJdLCJkaXNhYmxlZCI6W119LCJ2ZW5kb3JzIjp7ImRpc2FibGVkIjpbXSwiZW5hYmxlZCI6WyJjOmFkaW1vLVBoVVZtNkZFIiwiYzpyZXRlbmN5LUNMZXJaaUdMIiwiYzppbnRvd293aW4tcWF6dDV0R2kiLCJjOmFiLXRhc3R5IiwiYzphZG1vdGlvbiIsImM6dnVibGUtY01DSlZ4NGUiLCJjOnJvY2tlcmJveC1mVE04RUo5UCIsImM6cXdlcnRpemUtemRuZ0UyaHgiLCJjOnNjaGlic3RlZC1NUVBYYXF5aCIsImM6YWZmaWxpbmV0IiwiYzpkaWRvbWkiLCJjOnJldGFyZ2V0ZXItYmVhY29uIiwiYzpyYWR2ZXJ0aXMtU0pwYTI1SDgiLCJjOmdyZWVuaG91c2UtUUtiR0JrczQiLCJjOnlvcm1lZGlhcy1xbkJXaFF5UyIsImM6cHVycG9zZWxhLTN3NFpmS0tEIiwiYzpha2FtYWkiLCJjOmF0LWludGVybmV0IiwiYzptYXl0cmljc2ctQVMzNVlhbTkiLCJjOnJlc2VhcmNoLW5vdyIsImM6Y2FibGF0b2xpLW5SbVZhd3AyIiwiYzpoYXNvZmZlci04WXlNVHRYaSIsImM6cnRhcmdldC1HZWZNVnlpQyIsImM6dGhpcmRwcmVzZS1Tc0t3bUhWSyIsImM6cm9ja3lvdSIsImM6bGVtb21lZGlhLXpiWWhwMlFjIiwiYzptYXhjZG4taVVNdE5xY0wiLCJjOndoZW5ldmVybS04Vllod2IyUCIsImM6Y2xvdWRmbGFyZSIsImM6aW5mZWN0aW91cy1tZWRpYSIsImM6Y3JlYXRlanMiLCJjOmxrcWQtY1U5UW1CNlciLCJjOnNuYXBpbmMteWhZbkpaZlQiLCJjOnNmci1NZHBpN2tmTiIsImM6b3NjYXJvY29tLUZSY2hOZG5IIiwiYzpmb3J0dmlzaW9uLWllNmJYVHc5IiwiYzp6YW5veCIsImM6dmlhbnQtNDd4MlloZjciLCJjOmFkbGlnaHRuaS10V1pHcmVoVCIsImM6bW9iaWZ5IiwiYzp0dXJibyIsImM6aW50aW1hdGUtbWVyZ2VyIiwiYzppbGx1bWF0ZWMtQ2h0RUI0ZWsiLCJjOnJldmxpZnRlci1jUnBNbnA1eCIsImM6YWR2YW5zZS1INnFiYXhuUSIsImM6YXBwc2ZseWVyLVlyUGRHRjYzIiwiYzpsYmNmcmFuY2UiLCJjOmJyYW5jaC1WMmRFQlJ4SiIsImM6cmVhbHplaXRnLWI2S0NreHlWIiwiYzphZGp1c3RnbWItcGNjTmRKQlEiLCJjOnN3YXZlbi1MWUJyaW1BWiIsImM6anF1ZXJ5IiwiYzpzYW5vbWEiLCJjOnB1Ym9jZWFuLWI2QkpNdHNlIl19fQ==\" -H \"accept: application/json\" -H \"content-type: application/json\" -H \"api_key: ba0c2dad52b3ec\" -H \"accept-language: en-US,en;q=0.9\" -H \"user-agent: LBC;iOS;17.0.1;iPhone;phone;C31BDB8B-A4CC-46A7-9405-0024921AA166;wifi;6.106.0;20161220.1725.1\" --data-binary \"{\\\"filters\\\":{\\\"enums\\\":{\\\"ad_type\\\":[\\\"offer\\\"]}},\\\"limit\\\":6,\\\"limit_alu\\\":0,\\\"owner_type\\\":\\\"all\\\",\\\"sort_by\\\":\\\"time\\\",\\\"sort_order\\\":\\\"desc\\\"}\" --compressed \"https://api.leboncoin.fr/api/adfinder/v1/around_me\"");
        assertThat(args).containsExactly("curl",
            "--data-binary",
            "{\"somekey\":\"somevalue\"}",
            "--compressed",
            "https://api.leboncoin.fr/api/adfinder/v1/around_me");
    }

}
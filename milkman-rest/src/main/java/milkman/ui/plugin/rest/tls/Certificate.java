package milkman.ui.plugin.rest.tls;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Certificate {

  String base64Certificate;
  String base64PrivateKey;
  String name;
  CertificateType type;
}

package milkman.ui.plugin.rest.tls;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class CertificateReader {

  public X509Certificate readCertificate(Certificate certificate) throws IOException, CertificateException {

    if (certificate.getType() == CertificateType.PEM) {
      return readPem(certificate.getBase64Certificate());
    }

    throw new IllegalArgumentException("Unsupported certificate type: " + certificate.getType());
  }

  public PrivateKey readPrivateKey(Certificate certificate) throws Exception{

    if (certificate.getType() == CertificateType.PEM) {
      return readPemPrivateKey(certificate.getBase64PrivateKey());
    }

    throw new IllegalArgumentException("Unsupported certificate type: " + certificate.getType());
  }

  private PrivateKey readPemPrivateKey(String base64PrivateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
    String key = new String(Base64.getDecoder().decode(base64PrivateKey));
    String privateKeyPEM = key
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replaceAll(System.lineSeparator(), "")
        .replace("-----END PRIVATE KEY-----", "");

    byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);

    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
    return keyFactory.generatePrivate(keySpec);
  }

  private X509Certificate readPem(String base64Data) throws IOException, CertificateException {
    CertificateFactory fact = CertificateFactory.getInstance("X.509");
    try (ByteArrayInputStream input = new ByteArrayInputStream(Base64.getDecoder().decode(base64Data))) {
      return (X509Certificate) fact.generateCertificate(input);
    }
  }
}

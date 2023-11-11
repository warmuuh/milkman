package milkman.ui.plugin.rest;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import org.junit.jupiter.api.Test;

// https://adfinis.com/en/blog/openssl-x509-certificates/

public class CertLoadingTest {

  @Test
  public void shouldLoadPemFile() throws CertificateException, IOException {
    CertificateFactory fact = CertificateFactory.getInstance("X.509");
    var is = getClass().getResourceAsStream("/ssl/example.com.pem");
    X509Certificate cer = (X509Certificate) fact.generateCertificate(is);
    is.close();
    System.out.println(cer.getSubjectX500Principal());
  }

}

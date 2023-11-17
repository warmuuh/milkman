package milkman.ui.plugin.rest.tls;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Optional;
import net.christopherschultz.pemutils.PEMFile;
import net.christopherschultz.pemutils.PEMFile.CertificateEntry;
import net.christopherschultz.pemutils.PEMFile.Entry;
import net.christopherschultz.pemutils.PEMFile.PrivateKeyEntry;
import org.apache.commons.lang3.StringUtils;

public class CertificateReader {

  public X509Certificate readCertificate(Certificate certificate) throws Exception {

    if (certificate.getType() == CertificateType.PEM) {
      return readPem(certificate.getBase64Certificate());
    }

    throw new IllegalArgumentException("Unsupported certificate type: " + certificate.getType());
  }

  public PrivateKey readPrivateKey(Certificate certificate) throws Exception {

    if (certificate.getType() == CertificateType.PEM) {
      Optional<String> pw = Optional.ofNullable(certificate.getPassword()).filter(StringUtils::isNotBlank);
      return readPemPrivateKey(certificate.getBase64PrivateKey(), pw);
    }

    throw new IllegalArgumentException("Unsupported certificate type: " + certificate.getType());
  }

  private PrivateKey readPemPrivateKey(String base64PrivateKey, Optional<String> password) throws Exception {
    Collection<Entry> pemEntities = decode(new String(Base64.getDecoder().decode(base64PrivateKey)), password);
    return pemEntities.stream()
        .filter(PrivateKeyEntry.class::isInstance)
        .map(PrivateKeyEntry.class::cast)
        .map(PrivateKeyEntry::getPrivateKey)
        .findAny()
        .orElseThrow(() -> new IllegalStateException("No private key found"));
  }

  private X509Certificate readPem(String base64Data) throws Exception {
    Collection<Entry> pemEntities = decode(new String(Base64.getDecoder().decode(base64Data)), Optional.empty());
    return pemEntities.stream()
        .filter(CertificateEntry.class::isInstance)
        .map(CertificateEntry.class::cast)
        .map(CertificateEntry::getCertificate)
        .filter(X509Certificate.class::isInstance)
        .map(X509Certificate.class::cast)
        .findAny()
        .orElseThrow(() -> new IllegalStateException("No certificate found"));
  }

  public static Collection<Entry> decode(String pem, Optional<String> password) throws IOException, GeneralSecurityException {
    PEMFile pf = new PEMFile(new StringReader(pem));

    password.ifPresent(pw -> pf.setPasswordProvider(() -> pw));

    ArrayList<Entry> entries = new ArrayList<Entry>();
    Entry e;

    while ((e = pf.getNext()) != null) {
      entries.add(e);
    }

    return entries;
  }
}

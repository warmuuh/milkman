package milkman.ui.plugin.rest.tls;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;

public class CustomCertificateKeyManager extends X509ExtendedKeyManager {

  private final X509Certificate clientCertificate;
  private final PrivateKey privateKey;

  public CustomCertificateKeyManager(X509Certificate clientCertificate, PrivateKey privateKey) {
    this.clientCertificate = clientCertificate;
    this.privateKey = privateKey;
  }

  @Override
  public String[] getClientAliases(String keyType, Principal[] issuers) {
    return new String[0];
  }

  @Override
  public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
    return null;
  }

  @Override
  public String[] getServerAliases(String keyType, Principal[] issuers) {
    return new String[0];
  }

  @Override
  public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
    return null;
  }

  @Override
  public X509Certificate[] getCertificateChain(String alias) {
    return new X509Certificate[]{clientCertificate};
  }

  @Override
  public PrivateKey getPrivateKey(String alias) {
    return privateKey;
  }

  @Override
  public String chooseEngineClientAlias(String[] keyType, Principal[] issuers, SSLEngine engine) {
    return "test";
  }
}

package milkman.ctrl;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import milkman.domain.RequestContainer;
import milkman.domain.ResponseContainer;
import milkman.ui.main.Toaster;
import milkman.ui.main.options.CoreApplicationOptionsProvider;
import milkman.ui.plugin.ExecutionListenerAware.ExecutionListener;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class SocksProxyController {

  private final ExecutionListenerManager executionListenerManager;
  private final Toaster toaster;

  @PostConstruct
  public void setup() {
    executionListenerManager.listenOnExecution(new ExecutionListener() {
      @Override
      public void onRequestStarted(RequestContainer request, ResponseContainer response) {
        if (CoreApplicationOptionsProvider.options().isUseSocksProxy()) {
          String[] address = CoreApplicationOptionsProvider.options().getSocksProxyAddress().split(":");
          if (address.length != 2) {
            toaster.showToast("Unexpected Socks Proxy Address format");
          } else {
            System.setProperty("socksProxyPort", address[1]);
            System.setProperty("socksProxyHost", address[0]);
            //TODO: set socksNonProxyHosts
          }
        } else {
          System.clearProperty("socksProxyHost");
          System.clearProperty("socksProxyPort");
          //TODO: unset socksNonProxyHosts
        }
      }

      @Override
      public void onRequestReady(RequestContainer request, ResponseContainer response) {

      }

      @Override
      public void onRequestFinished(RequestContainer request, ResponseContainer response) {
        System.clearProperty("socksProxyHost");
        System.clearProperty("socksProxyPort");
      }
    });
  }
}

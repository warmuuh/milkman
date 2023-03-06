package milkman.plugin.cassandra.proxy;

import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.session.ProgrammaticArguments;
import com.datastax.oss.driver.internal.core.context.DefaultDriverContext;
import com.datastax.oss.driver.internal.core.context.DefaultNettyOptions;
import com.datastax.oss.driver.internal.core.context.NettyOptions;
import milkman.ui.main.options.CoreApplicationOptionsProvider;

public class SocksProxyAwareDriverContext extends DefaultDriverContext {
	public SocksProxyAwareDriverContext(DriverConfigLoader configLoader, ProgrammaticArguments programmaticArguments) {
		super(configLoader, programmaticArguments);
	}
 
	@Override
	protected NettyOptions buildNettyOptions() {
		if (CoreApplicationOptionsProvider.options().isUseSocksProxy()) {
			String[] socksProxyAddress = CoreApplicationOptionsProvider.options().getSocksProxyAddress().split(":");
			return new SocksProxyNettyOptions(this, socksProxyAddress[0], Integer.parseInt(socksProxyAddress[1]));
		} else {
			return new DefaultNettyOptions(this);
		}
	}
}
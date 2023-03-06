package milkman.plugin.cassandra.proxy;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.context.DriverContext;
import com.datastax.oss.driver.api.core.session.ProgrammaticArguments;
import com.datastax.oss.driver.api.core.session.SessionBuilder;

public class SocksProxyAwareSessionBuilder extends SessionBuilder<SocksProxyAwareSessionBuilder, CqlSession> {
 
	@Override
	protected DriverContext buildContext(DriverConfigLoader configLoader, ProgrammaticArguments programmaticArguments) {
		return new SocksProxyAwareDriverContext(configLoader, programmaticArguments);
	}
 
	@Override
	protected CqlSession wrap(CqlSession defaultSession) {
		return defaultSession;
	}
}
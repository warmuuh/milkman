package milkman.plugin.cassandra.proxy;

import com.datastax.oss.driver.internal.core.context.DefaultNettyOptions;
import com.datastax.oss.driver.internal.core.context.InternalDriverContext;
import io.netty.channel.Channel;
import io.netty.handler.proxy.Socks5ProxyHandler;
import java.net.InetSocketAddress;
import org.apache.commons.lang3.StringUtils;

public class SocksProxyNettyOptions extends DefaultNettyOptions {
	private String socksProxyHost;
	private Integer socksProxyPort;
 
	public SocksProxyNettyOptions(InternalDriverContext context, String socksProxyHost, Integer socksProxyPort) {
		super(context);
		this.socksProxyHost = socksProxyHost;
		this.socksProxyPort = socksProxyPort;
	}
 
	@Override
	public void afterChannelInitialized(Channel channel) {
		if (!StringUtils.isEmpty(socksProxyHost) && socksProxyPort != null && socksProxyPort > 0) {
			channel.pipeline().addFirst(new Socks5ProxyHandler(new InetSocketAddress(socksProxyHost, socksProxyPort)));
		}
	}
}
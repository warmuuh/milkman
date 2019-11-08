package milkman.ui.plugin.rest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.protocol.HttpContext;

public class ProxyExclusionRoutePlanner  {

	private List<String> proxyExclusionList;
	private URL proxyUrl;

	public ProxyExclusionRoutePlanner(URL proxyUrl, String proxyList) {
		this.proxyUrl = proxyUrl;
		this.proxyExclusionList = Arrays.asList(proxyList.split("\\|"));
	}
	
	
	private boolean isExluded(String hostname) {
		for(String proxyExclusion : proxyExclusionList) {
    		if (StringUtils.isNotBlank(proxyExclusion) 
    				&& hostname.toLowerCase().contains(proxyExclusion.toLowerCase())) {
    			return true;
    		}
    	}
		return false;
	}
	
	public DefaultProxyRoutePlanner apache() {
		return new DefaultProxyRoutePlanner(new HttpHost(proxyUrl.getHost(), proxyUrl.getPort())) {
			@Override
		    public HttpRoute determineRoute(
		            final HttpHost host,
		            final HttpRequest request,
		            final HttpContext context) throws HttpException {
		        if (isExluded(host.getHostName())) {
		    			return new HttpRoute(host); //direct route
		    	}
		        return super.determineRoute(host, request, context);
		    }
		};
	}
	
	
	public ProxySelector java() {
		return new ProxySelector() {
			@Override
			public List<Proxy> select(URI uri) {
				if (isExluded(uri.getHost())) {
					return List.of(Proxy.NO_PROXY); //direct route
				}

				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyUrl.getHost(), proxyUrl.getPort()));
				return List.of(proxy);
			}
			
			@Override
			public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
				throw new RuntimeException("Failed to connect to proxy: " + sa, ioe);
			}
		};
	}
	

}

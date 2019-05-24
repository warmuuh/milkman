package milkman.ui.plugin.rest;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.protocol.HttpContext;

public class ProxyExclusionRoutePlanner extends DefaultProxyRoutePlanner {

	private List<String> proxyList;

	public ProxyExclusionRoutePlanner(HttpHost proxy, String proxyList) {
		super(proxy);
		this.proxyList = Arrays.asList(proxyList.split("\\|"));
	}
	
	@Override
    public HttpRoute determineRoute(
            final HttpHost host,
            final HttpRequest request,
            final HttpContext context) throws HttpException {
        String hostname = host.getHostName();
    	for(String proxyExclusion : proxyList) {
    		if (StringUtils.isNotBlank(proxyExclusion) 
    				&& hostname.toLowerCase().contains(proxyExclusion.toLowerCase())) {
    			return new HttpRoute(host); //direct route
    		}
    	}
        return super.determineRoute(host, request, context);
    }

}

package milkman.ui.plugin.rest;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Base64;

import milkman.ui.plugin.Templater;
import milkman.ui.plugin.rest.domain.RestRequestContainer;

public class HttpUtil {

	public static String extractContentType(String headerValue) {
		int idx = headerValue.indexOf(';');
		if (idx >= 0)
			return headerValue.substring(0, idx);
		
		return headerValue;
	}
	
	public static String escapeUrl(RestRequestContainer request, Templater templater)
			throws MalformedURLException, URISyntaxException {
		String finalUrl = templater.replaceTags(request.getUrl());
		URL url = new URL(URLDecoder.decode(finalUrl));
	    URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
		// fix missing encoding for [ and ]
	    return uri.toASCIIString()
				.replace("[", "%5B")
				.replace("]", "%5D");
	}
	
	public static String authorizationHeaderValue(PasswordAuthentication creds) {
		String value = creds.getUserName() + ":" + new String(creds.getPassword());
		return "Basic " + Base64.getEncoder().encodeToString(value.getBytes());
	}

}

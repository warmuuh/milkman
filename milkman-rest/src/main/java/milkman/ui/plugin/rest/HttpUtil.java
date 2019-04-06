package milkman.ui.plugin.rest;

public class HttpUtil {

	public static String extractContentType(String headerValue) {
		int idx = headerValue.indexOf(';');
		if (idx >= 0)
			return headerValue.substring(0, idx);
		
		return headerValue;
	}
}

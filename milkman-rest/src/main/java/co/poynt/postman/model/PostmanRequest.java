package co.poynt.postman.model;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostmanRequest {
	public String method;
	public List<PostmanHeader> header;
	public PostmanBody body;
	public PostmanUrl url;

	public String getData(PostmanVariables var) {
		if (body == null || body.mode == null) {
			return "";
		} else {
			switch (body.mode) {
			case "raw":
				return var.replace(body.raw);
			case "urlencoded":
				return urlFormEncodeData(var, body.urlencoded);
			default:
				return "";
			}
		}
	}

	public String urlFormEncodeData(PostmanVariables var, List<PostmanUrlEncoded> formData) {
		String result = "";
		int i = 0;
		for (PostmanUrlEncoded encoded : formData) {
			result += encoded.key + "=" + URLEncoder.encode(var.replace(encoded.value));
			if (i < formData.size() - 1) {
				result += "&";
			}
		}
		return result;
	}

	public String getUrl(PostmanVariables var) {
		return var.replace(url.raw);
	}

	public Map<String, String> getHeaders(PostmanVariables var) {
		Map<String, String> result = new HashMap<>();
		if (header == null || header.isEmpty()) {
			return result;
		}
		for (PostmanHeader head : header) {
			result.put(head.key, var.replace(head.value));
		}
		return result;
	}
}

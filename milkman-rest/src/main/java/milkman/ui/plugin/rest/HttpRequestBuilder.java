package milkman.ui.plugin.rest;

import java.util.List;
import java.util.Map;

import lombok.Data;

public interface HttpRequestBuilder {

	void addHeader(String key, String value);
	void setBody(String body);
}

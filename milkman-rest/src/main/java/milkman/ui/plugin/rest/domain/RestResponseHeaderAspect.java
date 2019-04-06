package milkman.ui.plugin.rest.domain;

import java.util.LinkedList;
import java.util.List;

import lombok.Data;
import milkman.domain.ResponseAspect;
import milkman.ui.plugin.rest.HttpUtil;

@Data
public class RestResponseHeaderAspect implements ResponseAspect {
	
	private List<HeaderEntry> entries = new LinkedList<>();
	
	public String contentType() {
		return entries.stream()
				.filter(e -> e.getName().equalsIgnoreCase("content-type"))
				.map(e -> HttpUtil.extractContentType(e.getValue()))
				.findAny().orElse("text/plain");
	}
}

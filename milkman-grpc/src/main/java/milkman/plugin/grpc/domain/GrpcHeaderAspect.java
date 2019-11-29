package milkman.plugin.grpc.domain;

import java.util.LinkedList;
import java.util.List;

import lombok.Data;
import milkman.domain.RequestAspect;
import milkman.ui.plugin.Templater;

@Data
public class GrpcHeaderAspect extends RequestAspect {
	private List<HeaderEntry> entries = new LinkedList<>();
	
	public GrpcHeaderAspect() {
		super("headers");
	}
	
//	@Override
//	public void enrichRequest(HttpRequestBuilder builder, Templater templater) throws Exception {
//		entries.stream()
//			.filter(HeaderEntry::isEnabled)
//			.forEach(h -> builder.addHeader(templater.replaceTags(h.getName()), templater.replaceTags(h.getValue())));
//	}
}

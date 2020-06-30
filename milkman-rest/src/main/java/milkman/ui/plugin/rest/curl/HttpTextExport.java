package milkman.ui.plugin.rest.curl;

import lombok.RequiredArgsConstructor;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestBodyAspect;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;

@RequiredArgsConstructor
public class HttpTextExport implements TextExport {

    @Override
    public String export(boolean isWindows, RestRequestContainer request, Templater templater) {
        StringBuilder b = new StringBuilder();

        var uri = URI.create(templater.replaceTags(request.getUrl()));

        b.append(request.getHttpMethod().toUpperCase())
                .append(" ");

        if (StringUtils.isNotBlank(uri.getPath())){
            b.append(uri.getPath());
        } else {
            b.append("/");
        }

        if (StringUtils.isNotBlank(uri.getQuery())) {
            b.append("?").append(uri.getQuery());
        }
        b.append('\n');
        appendHeader(b, "Host", uri.getHost());

        var headerAspect = request.getAspect(RestHeaderAspect.class).orElseThrow(() -> new IllegalArgumentException("missing aspect"));

        for (HeaderEntry entry : headerAspect.getEntries()) {
            appendHeader(b, entry.getName(), entry.getValue());
        }

        var bodyAspect = request.getAspect(RestBodyAspect.class).orElseThrow(() -> new IllegalArgumentException("missing aspect"));
        if (StringUtils.isNotBlank(bodyAspect.getBody())) {
            b.append("\n");
            b.append(bodyAspect.getBody());
        }

        return templater.replaceTags(b.toString());
    }

    private void appendHeader(StringBuilder b,String name, String value) {
        b.append(name).append(": ").append(value).append("\n");
    }
}

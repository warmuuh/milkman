package milkman.ui.plugin.rest.curl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.TextExport;
import milkman.ui.plugin.rest.RequestBodyPostProcessor;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestBodyAspect;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@RequiredArgsConstructor
public class HttpTextExport implements TextExport<RestRequestContainer> {

    @Override
    public String export(boolean isWindows, RestRequestContainer request, Templater templater) {
        StringBuilder b = new StringBuilder();

        try {
            var uri = new URL(templater.replaceTags(request.getUrl()));

            b.append(request.getHttpMethod().toUpperCase())
                    .append(" ");

            if (StringUtils.isNotBlank(uri.getPath())) {
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
                if (entry.isEnabled()) {
                    appendHeader(b, entry.getName(), entry.getValue());
                }
            }

            var bodyAspect = request.getAspect(RestBodyAspect.class).orElseThrow(() -> new IllegalArgumentException("missing aspect"));
            if (StringUtils.isNotBlank(bodyAspect.getBody())) {
                b.append("\n");
                var processedBodyContent = getContentType(request)
                        .map(contentType -> RequestBodyPostProcessor.processBody(contentType, bodyAspect.getBody()))
                        .orElse(bodyAspect.getBody());
                b.append(processedBodyContent);
            }

            return templater.replaceTags(b.toString());
        } catch (MalformedURLException e) {
            log.warn("failed to export http", e);
            return "export failed: " + e.getMessage();
        }

    }

    private void appendHeader(StringBuilder b,String name, String value) {
        b.append(name).append(": ").append(value).append("\n");
    }

    private Optional<String> getContentType(RestRequestContainer request) {
        return request.getAspect(RestHeaderAspect.class)
                .flatMap(h -> h.getEntries().stream()
                        .filter(e -> e.getValue().equalsIgnoreCase("content-type"))
                        .map(HeaderEntry::getValue)
                        .findAny());
    }
}

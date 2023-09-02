package milkman.ui.plugin.rest.curl;

import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.TextExport;
import milkman.ui.plugin.rest.RequestBodyPostProcessor;
import milkman.ui.plugin.rest.domain.HeaderEntry;
import milkman.ui.plugin.rest.domain.RestBodyAspect;
import milkman.ui.plugin.rest.domain.RestHeaderAspect;
import milkman.ui.plugin.rest.domain.RestRequestContainer;
import org.apache.commons.lang3.StringUtils;

@RequiredArgsConstructor
public class CurlTextExport implements TextExport<RestRequestContainer> {

    @Override
    public String export(boolean isWindows, RestRequestContainer request, Templater templater) {

        String lineBreak = isWindows ? " ^\n  " : " \\\n  ";
        String quote = isWindows ? "\"" : "'";

        StringBuilder b = new StringBuilder();
        b.append("curl -X ");
        b.append(request.getHttpMethod());
        b.append(lineBreak);
        b.append(quote+request.getUrl()+quote);


        request.getAspect(RestHeaderAspect.class).ifPresent(a -> {
            if (!a.getEntries().isEmpty())
                b.append(lineBreak);
            String headers = a.getEntries().stream()
                    .filter(HeaderEntry::isEnabled)
                    .map(h -> "-H " + quote + h.getName() + ": " + h.getValue() + quote)
                    .collect(Collectors.joining(lineBreak));
            b.append(headers);
        });

        request.getAspect(RestBodyAspect.class).ifPresent(a -> {
            if (StringUtils.isNotBlank(a.getBody())) {
                b.append(lineBreak);
                var processedBodyContent = getContentType(request)
                        .map(contentType -> RequestBodyPostProcessor.processBody(contentType, a.getBody()))
                        .orElse(a.getBody());
                String normalized = StringUtils.normalizeSpace(processedBodyContent);
                if (isWindows){
                    normalized = normalized.replaceAll("\"", "\"\""); // replace all quotes with double quotes
                }
                b.append("-d " + quote);b.append(normalized);b.append(quote);
            }
        });



        return templater.replaceTags(b.toString());
    }

    private Optional<String> getContentType(RestRequestContainer request) {
        return request.getAspect(RestHeaderAspect.class)
                .flatMap(h -> h.getEntries().stream()
                        .filter(e -> e.getName().equalsIgnoreCase("content-type"))
                        .map(HeaderEntry::getValue)
                        .findAny());
    }
}

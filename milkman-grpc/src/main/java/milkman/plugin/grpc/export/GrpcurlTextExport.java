package milkman.plugin.grpc.export;

import milkman.plugin.grpc.domain.*;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.TextExport;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Collectors;

public class GrpcurlTextExport implements TextExport<GrpcRequestContainer> {
    @Override
    public String export(boolean isWindows, GrpcRequestContainer request, Templater templater) {
        String lineBreak = isWindows ? " ^\n  " : " \\\n  ";
        String quote = isWindows ? "\"" : "'";

        StringBuilder b = new StringBuilder();
        b.append("grpcurl ");

        request.getAspect(GrpcHeaderAspect.class).ifPresent(a -> {
            if (!a.getEntries().isEmpty()) {
                b.append(lineBreak);
                String headers = a.getEntries().stream()
                        .filter(HeaderEntry::isEnabled)
                        .map(h -> "-H " + quote + h.getName() + ": " + h.getValue() + quote)
                        .collect(Collectors.joining(lineBreak));
                b.append(headers);
                b.append(lineBreak);
            }
        });

        request.getAspect(GrpcPayloadAspect.class).ifPresent(a -> {
            if (StringUtils.isNotBlank(a.getPayload())) {
                b.append(lineBreak);
                String normalized = StringUtils.normalizeSpace(a.getPayload());
                if (isWindows) {
                    normalized = normalized.replaceAll("\"", "\"\""); // replace all quotes with double quotes
                }
                b.append("-d " + quote);
                b.append(normalized);
                b.append(quote);
                b.append(lineBreak);
            }
        });

        b.append(request.getEndpoint());
        b.append(lineBreak);
        request.getAspect(GrpcOperationAspect.class).ifPresent(operation -> {
            b.append(operation.getOperation());
        });

        return templater.replaceTags(b.toString());
    }
}

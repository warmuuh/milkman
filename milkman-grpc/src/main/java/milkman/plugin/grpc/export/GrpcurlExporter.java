package milkman.plugin.grpc.export;

import milkman.plugin.grpc.domain.GrpcRequestContainer;
import milkman.ui.plugin.AbstractTextExporter;

public class GrpcurlExporter extends AbstractTextExporter<GrpcRequestContainer> {
    public GrpcurlExporter() {
        super("gRPCurl", new GrpcurlTextExport());
    }
}

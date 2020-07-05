package milkman.plugin.cassandra.export;

import milkman.plugin.cassandra.domain.CassandraRequestContainer;
import milkman.ui.plugin.AbstractTextExporter;

public class CqlshExporter extends AbstractTextExporter<CassandraRequestContainer> {
    public CqlshExporter() {
        super("Cqlsh", new CqlshTextExport());
    }
}

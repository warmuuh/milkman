package milkman.plugin.cassandra.export;

import milkman.plugin.cassandra.CassandraConnectionProperties;
import milkman.plugin.cassandra.domain.CassandraRequestContainer;
import milkman.plugin.jdbc.domain.JdbcSqlAspect;
import milkman.ui.plugin.Templater;
import milkman.ui.plugin.TextExport;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class CqlshTextExport implements TextExport<CassandraRequestContainer> {
    @Override
    public String export(boolean isWindows, CassandraRequestContainer request, Templater templater) {

        String lineBreak = isWindows ? " ^\n  " : " \\\n  ";
        String quote = isWindows ? "\"" : "'";

        var url = URI.create(templater.replaceTags(request.getCassandraUrl()));
        var properties = CassandraConnectionProperties.fromUri(url);

        StringBuilder b = new StringBuilder();
        b.append("cqlsh ");
        if (isNotBlank(properties.getUser())) {
            b.append("-u ").append(properties.getUser()).append(lineBreak);
        }
        if (isNotBlank(properties.getPassword())) {
            b.append("-p ").append(properties.getPassword()).append(lineBreak);
        }

        if (isNotBlank(properties.getKeyspace())){
            b.append("-k ").append(properties.getKeyspace()).append(lineBreak);
        }


        request.getAspect(JdbcSqlAspect.class).ifPresent(a -> {
            if (isNotBlank(a.getSql())) {
                b.append(lineBreak);
                String normalized = StringUtils.normalizeSpace(a.getSql());
                if (isWindows){
                    normalized = normalized.replaceAll("\"", "\"\""); // replace all quotes with double quotes
                }
                b.append("-e ").append(quote).append(normalized).append(quote).append(lineBreak);
            }
        });

        b.append(properties.getHost()).append(" ").append(properties.getPort());


        return templater.replaceTags(b.toString());
    }
}

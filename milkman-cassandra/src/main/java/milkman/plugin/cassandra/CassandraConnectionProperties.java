package milkman.plugin.cassandra;

import lombok.Value;

import java.net.URI;
import java.util.Optional;

import static milkman.plugin.cassandra.UrlUtil.splitQuery;

@Value
public class CassandraConnectionProperties {
    String datacenter;
    String user;
    String password;
    String keyspace;
    String host;
    int port;

    public static CassandraConnectionProperties fromUri(URI url) {
        var params = splitQuery(url);
        String datacenter = params.get("dc");
        if (datacenter == null) {
            throw new IllegalArgumentException("Missing datacenter name (parameter 'dc')");
        }

        String username = params.get("username");
        String password = params.get("password");

        String cassandraHost = url.getHost();
        int port = url.getPort() < 0 ? 9042 : url.getPort();
        String keyspace = Optional.ofNullable(url.getPath()).map(p -> p.substring(1)).orElse("");

        return new CassandraConnectionProperties(datacenter, username, password, keyspace, cassandraHost, port);
    }

}

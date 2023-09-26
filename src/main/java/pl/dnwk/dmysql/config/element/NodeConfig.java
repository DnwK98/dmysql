package pl.dnwk.dmysql.config.element;

public class NodeConfig {
    public String url = "localhost";
    public String user = "root";
    public String password = "";
    public String schema = "test";

    public static NodeConfig create() {
        return new NodeConfig();
    }

    public NodeConfig setUrl(String url) {
        this.url = "jdbc:" + url;
        return this;
    }

    public NodeConfig setUser(String user) {
        this.user = user;
        return this;
    }

    public NodeConfig setPassword(String password) {
        this.password = password;
        return this;
    }

    public NodeConfig setSchema(String schema) {
        this.schema = schema;
        return this;
    }
}

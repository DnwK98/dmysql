package pl.dnwk.dmysql.config;

import pl.dnwk.dmysql.sharding.schema.DistributedSchema;

public class Config {
    public int port = 9090;
    public ClusterConfig cluster = new ClusterConfig();

    // TODO Ensure this should be loaded from config
    public DistributedSchema schema = new DistributedSchema();
}

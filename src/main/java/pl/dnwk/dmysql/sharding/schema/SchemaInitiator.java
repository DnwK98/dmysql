package pl.dnwk.dmysql.sharding.schema;


import pl.dnwk.dmysql.cluster.Nodes;
import pl.dnwk.dmysql.common.Log;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class SchemaInitiator {

    public void initialize(Nodes nodes, DistributedSchema schema) {
        var result = nodes.executeQuery(onAll(nodes, "SHOW TABLES"));

        try {
            for (String nodeName : result.keySet()) {
                ResultSet resultSet = result.get(nodeName);

                if(!resultSet.next()) {
                    // There are no tables in schema, initialize node
                    Log.info("Initialize node: " + nodeName);
                    initializeNode(nodes, nodeName, schema);

                }
            }
        } catch (SQLException e) {
            Log.error("Failed to initiate DB: " + e.getMessage());
        }
    }

    private void initializeNode(Nodes nodes, String nodeName, DistributedSchema schema) {
        for (Table table : schema.tables.values()) {
            nodes.executeStatement(new HashMap<>(){{put(nodeName, table.getCreateStatement());}});;
        }
    }

    public HashMap<String, String> onAll(Nodes nodes, String statement) {
        var map = new HashMap<String , String>();

        for(var nodeName: nodes.names()) {
            map.put(nodeName, statement);
        }

        return map;
    }
}

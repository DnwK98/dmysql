package pl.dnwk.dmysql.sharding.schema;

import java.util.HashMap;
import java.util.Map;

public class DistributedSchema {

    public Map<String, Table> tables = new HashMap<>();

    public void add(Table table) {
        tables.put(table.tableName, table);
    }

    public Table get(String tableName) {
        if(!tables.containsKey(tableName)) {
            throw new RuntimeException("Missing table in DistributedSchema: " + tableName);
        }
        return tables.get(tableName);
    }
}

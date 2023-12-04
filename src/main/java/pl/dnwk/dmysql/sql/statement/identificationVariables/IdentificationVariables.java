package pl.dnwk.dmysql.sql.statement.identificationVariables;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class IdentificationVariables implements Serializable {

    Map<String, Table> aliasTableMap = new HashMap<>();
    Map<String, Field> aliasFieldMap = new HashMap<>();

    public void addTable(Table table) {
        aliasTableMap.put(table.alias, table);
    }

    public void addField(Field field) {
        aliasFieldMap.put(field.alias, field);
    }

    public Field getField(String path) {
        if (path.contains(".")) {
            String[] pathArray = path.split("\\.");
            for (var field : aliasFieldMap.values()) {
                if (field.table != null) {
                    if ((field.table.name != null && field.table.name.equals(pathArray[0])) || field.table.alias.equals(pathArray[0])) {
                        if (field.name.equals(pathArray[1])) {
                            return field;
                        }
                    }
                }
            }

            return new Field(getTable(pathArray[0]), pathArray[1], pathArray[1], null);
        } else {
            if (aliasFieldMap.containsKey(path)) {
                return aliasFieldMap.get(path);
            }
            for (var field : aliasFieldMap.values()) {
                if (field.name != null && field.name.equals(path)) {
                    return field;
                }
            }
            for (var field : aliasFieldMap.values()) {
                if (Integer.toString(field.column).equals(path)) {
                    return field;
                }
            }
        }

        if (hasSingleTable()) {
            return new Field(getSingleTable(), path, path, null);
        }

        throw new RuntimeException("Missing field for path: " + path);
    }

    public Table getTable(String path) {
        if (aliasTableMap.containsKey(path)) {
            return aliasTableMap.get(path);
        }
        for (var table : aliasTableMap.values()) {
            if (table.name.equals(path)) {
                return table;
            }
        }

        throw new RuntimeException("Missing table for identity: " + path);
    }

    public boolean hasSingleTable() {
        return aliasTableMap.size() == 1;
    }

    public Table getSingleTable() {
        if (!hasSingleTable()) {
            throw new RuntimeException("Unexpected state. There is more than one table to select");
        }

        return aliasTableMap.values().toArray(new Table[0])[0];
    }

    public static class Table implements Serializable {
        public final String name;
        public final String alias;

        public Table(String table, String alias) {
            this.name = table;
            this.alias = alias != null ? alias : name;
        }
    }

    public static class Field implements Serializable {
        public final Table table;
        public final String name;
        public final String alias;
        public final Integer column;

        public Field(Table table, String field, String alias, Integer column) {
            this.table = table;
            this.name = field;
            this.alias = alias != null ? alias : name;
            this.column = column;
        }
    }
}

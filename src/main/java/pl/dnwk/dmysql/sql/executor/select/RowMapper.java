package pl.dnwk.dmysql.sql.executor.select;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RowMapper {
    private final String[] columnTypes;

    public static RowMapper ofResult(ResultSet result) {
        try {
            int columnCount = result.getMetaData().getColumnCount();

            var types = new String[columnCount];
            for (int i = 0; i < columnCount; ++i) {
                types[i] = result.getMetaData().getColumnTypeName(i + 1);
            }

            return new RowMapper(types);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public RowMapper(String[] columnTypes) {
        this.columnTypes = columnTypes;
    }

    public Object[] mapRow(ResultSet result) {
        try {
            var row = new Object[columnTypes.length];
            for (int i = 0; i < columnTypes.length; ++i) {
                if (columnTypes[i].contains("INT")) {
                    row[i] = result.getInt(i + 1);;
                } else if (columnTypes[i].contains("DECIMAL")) {
                    row[i] = result.getFloat(i + 1);
                } else {
                    row[i] = result.getString(i + 1);
                }
            }

            return row;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

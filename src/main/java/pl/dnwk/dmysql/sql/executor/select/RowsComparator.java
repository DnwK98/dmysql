package pl.dnwk.dmysql.sql.executor.select;

import java.util.Comparator;

public class RowsComparator implements Comparator<Object[]> {

    private final ColumnDef[] columnDefs;

    public RowsComparator(ColumnDef[] columnDefs) {
        this.columnDefs = columnDefs;
    }

    public RowsComparator(ColumnDef columnDef) {
        this(new ColumnDef[]{columnDef});
    }

    public RowsComparator(Integer columnNum) {
        this(new ColumnDef[]{new ColumnDef(columnNum)});
    }


    public RowsComparator(Integer first, Integer second) {
        this(new ColumnDef[]{new ColumnDef(first), new ColumnDef(second)});
    }

    @Override
    public int compare(Object[] first, Object[] second) {
        var res = 0;
        for (var columnDef : columnDefs) {
            var columnNum = columnDef.columnNum;
            var multiplier = columnDef.desc ? -1 : 1;
            if (first[columnNum] instanceof String || second[columnNum] instanceof String) {
                res = multiplier * String.CASE_INSENSITIVE_ORDER.compare(
                        first[columnNum] != null ? (String) first[columnNum] : "",
                        second[columnNum] != null ? (String) second[columnNum] : ""
                );
            }
            if (first[columnNum] instanceof Integer || second[columnNum] instanceof Integer) {
                res = multiplier * Integer.compare(
                        first[columnNum] != null ? (Integer) first[columnNum] : Integer.MIN_VALUE,
                        second[columnNum] != null ? (Integer) second[columnNum] : Integer.MIN_VALUE
                );
            }
            if (first[columnNum] instanceof Float || second[columnNum] instanceof Float) {
                res = multiplier * Float.compare(
                        first[columnNum] != null ? (Float) first[columnNum] : Float.MIN_VALUE,
                        second[columnNum] != null ? (Float) second[columnNum] : Float.MIN_VALUE
                );
            }
            if (first[columnNum] instanceof Double || second[columnNum] instanceof Double) {
                res = multiplier * Double.compare(
                        first[columnNum] != null ? (Double) first[columnNum] : Double.MIN_VALUE,
                        second[columnNum] != null ? (Double) second[columnNum] : Double.MIN_VALUE
                );
            }
            if (res != 0) {
                return res;
            }
        }

        return res;
    }

    public static class ColumnDef {
        public final int columnNum;
        public final boolean desc;

        public ColumnDef(int columnNum, boolean desc) {
            this.columnNum = columnNum;
            this.desc = desc;
        }

        public ColumnDef(int columnNum) {
            this.columnNum = columnNum;
            this.desc = false;
        }
    }
}

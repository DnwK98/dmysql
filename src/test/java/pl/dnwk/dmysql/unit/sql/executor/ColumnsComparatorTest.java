package pl.dnwk.dmysql.unit.sql.executor;

import org.junit.jupiter.api.Test;

import pl.dnwk.dmysql.sql.executor.select.RowsComparator;
import pl.dnwk.dmysql.unit.UnitTestCase;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class ColumnsComparatorTest extends UnitTestCase {

    @Test
    public void sortsIntegerColumn() {
        var array = new Object[][]{
                {3}, {1}, {2},
        };

        Arrays.sort(array, new RowsComparator(0));

        var expected = new Object[][]{
                {1}, {2}, {3},
        };
        assertArrayEquals(expected, array);
    }

    @Test
    public void sortsIntegerColumnDescending() {
        var array = new Object[][]{
                {3}, {1}, {2},
        };

        Arrays.sort(array, new RowsComparator(new RowsComparator.ColumnDef(0, true)));

        var expected = new Object[][]{
                {3}, {2}, {1},
        };
        assertArrayEquals(expected, array);
    }

    @Test
    public void sortsStringColumn() {
        var array = new Object[][]{
                {"b"}, {"ba"}, {"ab"}, {"aa"}, {"a"}
        };

        Arrays.sort(array, new RowsComparator(0));

        var expected = new Object[][]{
                {"a"}, {"aa"}, {"ab"}, {"b"}, {"ba"}
        };

        assertArrayEquals(expected, array);
    }

    @Test
    public void sortTwoColumns() {
        var array = new Object[][]{
                {2, 9}, {1, 10}, {1, 9}, {3, 9}, {3, 5}
        };

        Arrays.sort(array, new RowsComparator(0, 1));

        var expected = new Object[][]{
                {1, 9}, {1, 10}, {2, 9}, {3, 5}, {3, 9}
        };

        assertArrayEquals(expected, array);
    }

    @Test
    public void sortWithNull() {
        var array = new Object[][]{
                {5}, {null}, {2}
        };

        Arrays.sort(array, new RowsComparator(0));

        var expected = new Object[][]{
                {null}, {2}, {5}
        };

        assertArrayEquals(expected, array);
    }
}

package pl.dnwk.dmysql.common;

import java.util.Arrays;

public class ArrayBuilder<T> {
    private int length = 0;
    private T[] data;

    public static ArrayBuilder<Object> create() {
        return new ArrayBuilder<>(new Object[128]);
    }

    public static ArrayBuilder<Object[]> create2D() {
        return new ArrayBuilder<>(new Object[128][]);
    }

    public static <I> ArrayBuilder<I> create(I[] initial) {
        return new ArrayBuilder<>(initial);
    }

    private ArrayBuilder(T[] d) {
        data = d;
    }

    public T[] toArray() {
        return Arrays.copyOf(data, length);
    }

    public ArrayBuilder<T> add(T element) {
        if (data.length == length) {
            data = Arrays.copyOf(data, 2 * (length + 1));
        }
        data[length++] = element;

        return this;
    }

    public ArrayBuilder<T> addAll(T[] elements) {
        if(length + elements.length >= data.length) {
            data = Arrays.copyOf(data, 2 * (data.length + elements.length));
        }

        for (var el : elements) {
            data[length++] = el;
        }

        return this;
    }

    public boolean empty() {
        return length == 0;
    }
}

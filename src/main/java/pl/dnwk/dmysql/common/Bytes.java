package pl.dnwk.dmysql.common;

import java.util.function.Consumer;

public class Bytes {

    private final byte[] internalBytes;
    int end = 0;

    public Bytes(int maxSize) {
        internalBytes = new byte[maxSize];
    }

    public void clear() {
        end = 0;
    }

    public void append(byte b) {
        internalBytes[end++] = b;
    }

    public void append(byte[] bytes) {
        System.arraycopy(bytes, 0, internalBytes, end, bytes.length);
        end += (bytes.length);
    }

    public void forEach(Consumer<Byte> action) {
        for (int i = 0; i < end; ++i) {

            action.accept(internalBytes[i]);
        }
    }

    public byte[] toArray() {
        byte[] response = new byte[end];
        System.arraycopy(internalBytes, 0, response, 0, end);

        return response;
    }

}

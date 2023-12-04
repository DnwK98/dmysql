package pl.dnwk.dmysql.common;

import java.io.*;

public class DeepCopy {

    @SuppressWarnings("unchecked")
    public static <T> T copy(T object) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            oos.flush();
            oos.close();
            bos.close();

            ByteArrayInputStream bais = new ByteArrayInputStream(bos.toByteArray());
            Object clonedObject = new ObjectInputStream(bais).readObject();
            bais.close();

            return (T) clonedObject;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

package pl.dnwk.dmysql.common;

public class Random {

    public static String string(int len) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        var random = new java.util.Random();

        return random.ints(leftLimit, rightLimit + 1)
                .limit(len)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}

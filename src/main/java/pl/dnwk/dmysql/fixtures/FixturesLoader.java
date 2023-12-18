package pl.dnwk.dmysql.fixtures;

import pl.dnwk.dmysql.common.Log;
import pl.dnwk.dmysql.connection.Connection;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FixturesLoader {
    public static void load(Connection connection) {
        InputStream is = FixturesLoader.class.getClassLoader().getResourceAsStream("fixtures/fixtures.sql");

        if(is == null) {
            return;
        }

        Log.info("Load fixtures...");
        new BufferedReader(new InputStreamReader(is))
                .lines()
                .forEach(connection::executeSql);
    }
}

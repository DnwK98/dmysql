package pl.dnwk.dmysql;

import pl.dnwk.dmysql.common.Log;
import pl.dnwk.dmysql.config.Config;


public class Main {
    public static void main(String[] args) {
        Log.setLevel(Log.INFO);

        Server server = new Server(new Config());
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        server.start();
        server.createSocket();
    }
}
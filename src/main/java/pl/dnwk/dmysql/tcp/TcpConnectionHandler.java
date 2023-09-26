package pl.dnwk.dmysql.tcp;

import pl.dnwk.dmysql.common.Bytes;

public interface TcpConnectionHandler {

    void init(Bytes output);

    int handle(Bytes input, Bytes output);

    void close();
}

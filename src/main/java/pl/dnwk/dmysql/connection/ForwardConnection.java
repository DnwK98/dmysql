package pl.dnwk.dmysql.connection;

import pl.dnwk.dmysql.Server;
import pl.dnwk.dmysql.common.Bytes;
import pl.dnwk.dmysql.common.Log;
import pl.dnwk.dmysql.common.Timer;
import pl.dnwk.dmysql.tcp.TcpConnectionHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ForwardConnection extends Connection implements TcpConnectionHandler {

    private Socket mySqlServer;
    private OutputStream serverInput;
    private InputStream serverOutput;

    public ForwardConnection(Server server) {
        super(server);
    }

    @Override
    public void init(Bytes output) {
        try {
            mySqlServer = new Socket("localhost", 3308);
            serverInput = mySqlServer.getOutputStream();
            serverOutput = mySqlServer.getInputStream();

            readBytes(output);
        } catch (IOException | InterruptedException e) {
            Log.error(e.getMessage());
        }
    }

    public void handle(Bytes input, Bytes output) {
        try {
            Timer.start("writeServer");
            serverInput.write(input.toArray());
            Timer.stop("writeServer");
            readBytes(output);
        } catch (IOException | InterruptedException e) {
            Log.error(e.getMessage());
        }
    }

    private void readBytes(Bytes bytes) throws IOException, InterruptedException {
        bytes.clear();

        // Handle client disconnection and read first byte
        Timer.start("waitForServer");
        Thread.sleep(0, 10);
        int resp = serverOutput.read();
        Timer.stop("waitForServer");
        if (resp == -1) {
            Log.info("Server disconnected");
            return;
        }

        bytes.append((byte) resp);

        Timer.start("readServer");

        // Read rest of bytes sent
        int readsWithoutData = 0;
        while (true) {
            int available = serverOutput.available();
            if (available > 0) {
                bytes.append(serverOutput.readNBytes(available));
                readsWithoutData = 0;
            } else {
                if (readsWithoutData++ > 5) {
                    break;
                }
            }
        }

        Timer.stop("readServer");
    }
}

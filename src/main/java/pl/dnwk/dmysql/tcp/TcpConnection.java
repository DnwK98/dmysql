package pl.dnwk.dmysql.tcp;

import pl.dnwk.dmysql.common.Bytes;
import pl.dnwk.dmysql.common.Log;
import pl.dnwk.dmysql.common.Timer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TcpConnection {

    private OutputStream output;
    private InputStream input;
    private final Socket socket;
    private TcpConnectionHandler handler;
    private boolean connected = true;

    public TcpConnection(Socket socket) {
        this.socket = socket;

        try {
            output = socket.getOutputStream();
            input = socket.getInputStream();
        } catch (IOException e) {
            Log.error(e.getMessage());
        }
    }

    public void handle() {
        try {
            Timer.start("connection");
            Bytes inputBytes = new Bytes(1024 * 1024);
            Bytes outputBytes = new Bytes(1024 * 1024);

            Log.info("Client connected " + socket.toString());

            Timer.start("init");
            handler.init(outputBytes);
            Timer.stop("init");
//            String s = new String(outputBytes.toArray(), StandardCharsets.UTF_8);
//            Log.debug("Server init: " + s);

            Timer.start("initWrite");
            output.write(outputBytes.toArray());
            Timer.stop("initWrite");

            while (connected) {
                readBytes(inputBytes);
//                s = new String(inputBytes.toArray(), StandardCharsets.UTF_8);
//                Log.debug("Client: " + s);

                handler.handle(inputBytes, outputBytes);
//                String r = new String(outputBytes.toArray(), StandardCharsets.UTF_8);
//                Log.debug("Server: " + r);

                Timer.start("writeClient");
                output.write(outputBytes.toArray());
                Timer.stop("writeClient");
            }

            handler.close();
            socket.close();
            Timer.stop("connection");
        } catch (IOException | InterruptedException e) {
            Log.error(e.getMessage());
        }
    }

    public void close()
    {
        this.connected = false;
    }

    private void readBytes(Bytes bytes) throws IOException, InterruptedException {
        bytes.clear();

        // Handle client disconnection and read first byte
        Timer.start("waitForClient");
        // This thread sleep is something weird. Without it thread goes to sleep on input.read()
        // for longer, so many little queries operates much slower.
        Thread.sleep(0, 10);
        int resp = input.read();
        Timer.stop("waitForClient");
        if (resp == -1) {
            Log.info("Client disconnected " + socket.toString());
            this.close();

            return;
        }
        bytes.append((byte)resp);

        Timer.start("readClient");

        // Read rest of bytes sent
        int readsWithoutData = 0;
        while (true) {
            int available = input.available();
            if (available > 0) {
                bytes.append(input.readNBytes(available));
                readsWithoutData = 0;
            } else {
                // TODO: Add end of message indication better than this
                if (readsWithoutData++ > 5) {
                    break;
                }
            }
        }

        Timer.stop("readClient");
    }

    public void setHandler(TcpConnectionHandler handler) {
        this.handler = handler;
    }
}

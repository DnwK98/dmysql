package pl.dnwk.dmysql;

import pl.dnwk.dmysql.common.Log;
import pl.dnwk.dmysql.common.Timer;
import pl.dnwk.dmysql.config.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class OldMain {
    public static void main(String[] args) throws InterruptedException {
        int port = 9090;

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            Log.info("Old server is listening on port " + port);

            while (true) {
                Socket client = serverSocket.accept();
                Socket server = new Socket("localhost", 3307);

                System.out.println("New client connected");

                OutputStream clientOutput = client.getOutputStream();
                InputStream clientInput = client.getInputStream();

                OutputStream serverInput = server.getOutputStream();
                InputStream serverOutput = server.getInputStream();

                // Read server
                while (true) {
                    //noinspection BusyWait
                    Thread.sleep(0, 10);
                    List<Byte> serverBuffer = new ArrayList<Byte>();
                    int timer = 0;
                    while (true) {
                        if (serverOutput.available() > 0) {
                            serverBuffer.add(serverOutput.readNBytes(1)[0]);
                            timer = 0;
                        } else {
                            if (timer++ > 2) {
                                break;
                            }
                        }
                    }

                    // Write console
                    byte[] bytes = new byte[serverBuffer.toArray().length];
                    int i = 0;
                    for (byte b : serverBuffer) {
                        bytes[i++] = b;
                    }
                    String s = new String(bytes, StandardCharsets.UTF_8);
                    if (s.length() > 0) {
                        System.out.print("Server: ");
                        System.out.println(s);
                    }

                    // Write client
                    for (byte b : serverBuffer) {
                        clientOutput.write(b);
                    }

                    // Read client
                    List<Byte> clientBuffer = new ArrayList<>();
                    timer = 0;
                    //noinspection BusyWait
                    Thread.sleep(0, 10);
                    while (true) {
                        if (clientInput.available() > 0) {
                            clientBuffer.add(clientInput.readNBytes(1)[0]);
                            timer = 0;
                        } else {
                            if (timer++ > 2) {
                                break;
                            }
                        }
                    }

                    // Write console
                    bytes = new byte[clientBuffer.toArray().length];
                    i = 0;
                    for (byte b : clientBuffer) {
                        bytes[i++] = b;
                    }
                    s = new String(bytes, StandardCharsets.UTF_8);
                    if (s.length() > 0) {
                        System.out.print("Client: ");
                        System.out.println(s);
                    }

                    // Write server
                    for (byte b : clientBuffer) {
                        serverInput.write(b);
                    }
                }
            }

        } catch (IOException | InterruptedException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
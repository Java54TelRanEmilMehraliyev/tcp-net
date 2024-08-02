package telran.net;
import java.net.*;
import java.util.concurrent.atomic.AtomicLong;
import java.io.*;

public class TcpClientServerSession extends Thread {
    Socket socket;
    Protocol protocol;
    AtomicLong lastActivityTime;
    TcpServer server;
    private static final long TIMEOUT = 60000; 

    public TcpClientServerSession(Socket socket, Protocol protocol, TcpServer server) {
        this.socket = socket;
        this.protocol = protocol;
        this.server = server;
        this.lastActivityTime = new AtomicLong(System.currentTimeMillis());

        try {
            socket.setSoTimeout(1000); // Set socket read timeout to 1 second
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try (BufferedReader receiver =
                new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintStream sender = new PrintStream(socket.getOutputStream())) {
            String line;

            while (true) {
                try {
                    if ((line = receiver.readLine()) != null) {
                        String responseStr = protocol.getResponseWithJSON(line);
                        sender.println(responseStr);
                        lastActivityTime.set(System.currentTimeMillis());
                    }
                } catch (SocketTimeoutException e) {
                    // Check for idle timeout
                    if (System.currentTimeMillis() - lastActivityTime.get() > TIMEOUT) {
                        System.out.println("Connection closed due to inactivity");
                        break;
                    }
                    // Check for server shutdown
                    if (!server.running) {
                        System.out.println("Server is shutting down");
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            close();
            server.removeSession(this); // Ensure the session is removed from the server's session list
        }
    }

    public void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
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
            socket.setSoTimeout(1000); // Установка таймаута для чтения сокета в 1 секунду
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
                    // Проверка таймаута простоя
                    if (System.currentTimeMillis() - lastActivityTime.get() > TIMEOUT) {
                        System.out.println("Соединение закрыто из-за бездействия");
                        break;
                    }
                    // Проверка на завершение работы сервера
                    if (!server.running) {
                        System.out.println("Сервер завершает работу");
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            close();
            server.removeSession(this);
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
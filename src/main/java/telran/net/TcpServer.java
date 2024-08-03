package telran.net;

import telran.io.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import telran.employees.Company;

public class TcpServer {
    Protocol protocol;
    int port;
    volatile boolean running = true;
    ServerSocket serverSocket;
    Company company;
    List<TcpClientServerSession> sessions = new CopyOnWriteArrayList<>();

    public TcpServer(Protocol protocol, int port) {
        this.protocol = protocol;
        this.port = port;
    }

    public void shutdown() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        saveCompanyData();
        for (TcpClientServerSession session : sessions) {
            session.close();
        }
    }

    private void saveCompanyData() {
        ((Persistable)company).save("employeesTest.data");
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(1000); 
            System.out.println("Сервер слушает на порту " + port);

            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    TcpClientServerSession session = new TcpClientServerSession(socket, protocol, this);
                    sessions.add(session);
                    session.start();
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Ошибка при принятии соединения: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            if (running) {
                e.printStackTrace();
            }
        } finally {
            shutdown();
        }
    }

    public void removeSession(TcpClientServerSession session) {
        sessions.remove(session);
    }
}
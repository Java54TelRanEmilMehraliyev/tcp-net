package telran.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
			if(serverSocket != null && !serverSocket.isClosed()) {
			serverSocket.close();	
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		saveCompanyData();
		for(TcpClientServerSession session : sessions) {
			session.close();
		}
	}
	private void saveCompanyData() {
		running = false;
		 try {
			 if(serverSocket != null) {
				 
			 }
		 }
		
		
	}
	public void run() {
		try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(1000);
            System.out.println("Server is listening on port " + port);

			while(running) {
				try {
				Socket socket = serverSocket.accept();
			
				TcpClientServerSession session =
						new TcpClientServerSession(socket, protocol, this);
				sessions.add(session);
				session.start();
				//DONE handling timeout exception
				 } catch (IOException e) {
	                    if (running) {
	                        System.err.println("Error accepting connection: " + e.getMessage());
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
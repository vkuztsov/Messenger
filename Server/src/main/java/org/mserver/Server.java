package org.mserver;

import org.mserver.network.ConnectionsManager;
import org.mserver.network.UserConnection;
import org.mserver.utility.ServerConfig;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerConfig serverConfig = new ServerConfig();
        ServerSocket serverSocket = new ServerSocket(serverConfig.getServerPort());
        ConnectionsManager manager = new ConnectionsManager();

        while(!serverSocket.isClosed()) {
            Socket socket = serverSocket.accept();
            UserConnection connection = new UserConnection(socket);
            manager.addConnection(connection);

            new Thread(connection).start();
        }
    }
}
package org.mserver.network;

import java.io.*;
import java.net.Socket;
public class UserConnection implements Runnable {

    private Socket socket;
    private RequestHandler requestHandler;

    public UserConnection(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        requestHandler = new RequestHandler(socket.getInetAddress().getHostAddress());

        try {
            BufferedReader input = null;
            BufferedWriter output = null;

            while(!socket.isClosed()) {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                String response = requestHandler.handle(input.readLine());
                output.write(response);
                output.flush();
            }

            input.close();
            output.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return socket;
    }
}

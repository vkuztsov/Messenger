package org.mserver.network;

import java.util.ArrayList;

public class ConnectionsManager {
    private ArrayList<UserConnection> connections = new ArrayList<>();

    public void addConnection(UserConnection connection) {
        connections.add(connection);
    }

    public ArrayList<UserConnection> getConnectionsList() {
        return connections;
    }
}

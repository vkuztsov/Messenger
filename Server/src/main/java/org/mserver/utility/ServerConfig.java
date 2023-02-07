package org.mserver.utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ServerConfig {
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private int serverPort;

    public int getServerPort() {
        return serverPort;
    }

    public ServerConfig() {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("server.config"));

            dbUrl = properties.getProperty("database.url");
            dbUser = properties.getProperty("database.user");
            dbPassword = properties.getProperty("database.password");
            serverPort = Integer.parseInt(properties.getProperty("port"));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }
}

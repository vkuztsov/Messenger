package org.mserver.database;

import java.sql.*;

public class Database {

    private Connection connection;
    private Timestamp timestamp;
    public Database(String url, String user, String password) {
        try {
            this.connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean addNewUser(String username, String firstName, String lastName, String email,
                           String password, String userPubKey, String regIp) {
        try {
            String query = "INSERT INTO users (username, first_name, last_name, email, password, " +
                    "user_pubkey, email_confirmed, registration_date, registration_ip) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, firstName);
            statement.setString(3, lastName);
            statement.setString(4, email);
            statement.setString(5, password);
            statement.setString(6, userPubKey);
            statement.setBoolean(7, false);
            statement.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
            statement.setString(9, regIp);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean userValueExist(String parameter, String value) {
        boolean valueExist = false;
        ResultSet resultSet;
        try {
            String query = "SELECT id FROM users WHERE " + parameter + " = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, value);
            resultSet = statement.executeQuery();

            valueExist = resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return valueExist;
    }
}

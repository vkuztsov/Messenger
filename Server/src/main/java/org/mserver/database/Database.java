package org.mserver.database;

import org.mserver.database.UserInfo;
import org.mserver.network.Session;

import java.sql.*;

public class Database {

    private final Connection connection;
    public Database(String url, String user, String password) {
        try {
            this.connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addNewUser(String username, String firstName, String lastName, String email,
                              String password, String userPubKey, String regIp) throws SQLException {
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
    }

    public boolean isUsernameExist(String username) throws SQLException {
        return searchData("users", "id", "username", username).next();
    }

    public boolean isEmailExist(String email) throws SQLException {
        return searchData("users", "id", "email", email).next();
    }

    public boolean userAuth(String login, String password) throws SQLException {
        ResultSet queryResult;
        if(login.contains("@")) {
            queryResult = searchData("users", "password", "email", login);
        } else {
            queryResult = searchData("users", "password", "username", login);
        }

        if(queryResult.next()) {
            if(password.equals(queryResult.getString("password")))
                return true;
        }

        return false;
    }

    public boolean addConfirmCode(int userId, String code) throws SQLException {
        String query = "INSERT INTO email_confirmation_codes (user_id, code) VALUES (?, ?)";
        PreparedStatement statement = connection.prepareStatement(query);

        statement.setInt(1, userId);
        statement.setString(2, code);

        return statement.execute();
    }

    public boolean addNewSession(Session session) throws SQLException {
        String query = "INSERT INTO auth_keys (user_id, initial_key, auth_key, user_ip, last_visit) " +
                "values (?, ?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(query);

        statement.setInt(1, session.getUserId());
        statement.setString(2, session.getInitialKey());
        statement.setString(3, session.getSessionKey());
        statement.setString(4, session.getUserIp());
        statement.setTimestamp(5, session.getLastVisit());

        return statement.execute();
    }

    public Session getSession(String authKey) throws SQLException {
        ResultSet resultSet = searchData("auth_keys", "*", "auth_key", authKey);

        if(resultSet.next()) {
            return new Session(
                    resultSet.getInt("user_id"),
                    resultSet.getString("initial_key"),
                    resultSet.getString("auth_key"),
                    resultSet.getString("user_ip"),
                    resultSet.getTimestamp("last_visit")
            );
        }

        return null;
    }

    public void setTwoFa(int userId, String secret) throws SQLException {
        String query = "UPDATE users SET totp_secret = ? WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(query);

        statement.setString(1, secret);
        statement.setInt(2, userId);

        statement.execute();
    }

    public UserInfo getUserInfo(String login) throws SQLException {
        ResultSet queryResult;
        if(login.contains("@")) {
            queryResult = searchData("users", "*", "email", login);
        } else {
            queryResult = searchData("users", "*", "username", login);
        }

        if(queryResult.next()) {
            UserInfo userInfo = new UserInfo(
                    queryResult.getInt("id"),
                    queryResult.getString("username"),
                    queryResult.getString("first_name"),
                    queryResult.getString("last_name"),
                    queryResult.getString("email"),
                    queryResult.getString("profile_photo_url"),
                    queryResult.getString("user_pubkey"),
                    queryResult.getString("totp_secret")
            );

            return userInfo;
        }

        return null;
    }

    public UserInfo getUserInfo(int userId) throws SQLException {
        ResultSet queryResult = searchData("users", "*", "id", userId);
        if(queryResult.next()) {
            UserInfo userInfo = new UserInfo(
                    queryResult.getInt("id"),
                    queryResult.getString("username"),
                    queryResult.getString("first_name"),
                    queryResult.getString("last_name"),
                    queryResult.getString("email"),
                    queryResult.getString("profile_photo_url"),
                    queryResult.getString("user_pubkey"),
                    queryResult.getString("totp_secret")
            );

            return userInfo;
        }

        return null;
    }

    private ResultSet searchData(String table, String parameter, String where, Object desired) throws SQLException {
        String query = String.format("SELECT %s FROM %s WHERE %s = ?", parameter, table, where);
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setObject(1, desired);

        return preparedStatement.executeQuery();
    }
}

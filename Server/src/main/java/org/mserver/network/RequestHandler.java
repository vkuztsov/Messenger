package org.mserver.network;

import org.mserver.database.Database;
import org.mserver.security.Cryptography;
import org.mserver.utility.ActionList;
import org.mserver.utility.ErrorList;
import org.mserver.utility.ServerConfig;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;

public class RequestHandler {
    private Database db;
    private String userIp;

    private Cryptography crypto;
    private Timestamp timestamp;

    public RequestHandler(String userIp) {
        this.userIp = userIp;
        ServerConfig serverConfig = new ServerConfig();

        crypto = new Cryptography();
        db = new Database(serverConfig.getDbUrl(),
                serverConfig.getDbUser(), serverConfig.getDbPassword());
        timestamp = new Timestamp(System.currentTimeMillis());
    }
    public String handle(String jsonRequest) {
        JSONObject result = new JSONObject();
        try {
            JSONObject json = new JSONObject(jsonRequest);
            int actionId = json.getInt("actionId");

            switch (actionId)
            {
                case ActionList.REGISTRATION:
                    result = registration(json);
                    break;
            }

        } catch (JSONException e) {
            return errorMessage(ErrorList.INCORRECT_REQUEST_FORM).toString();
        }

        return result.toString();
    }

    private JSONObject registration(JSONObject json) {
        boolean correct_data = true;
        try {
            String username = json.getString("username").toLowerCase();
            String firstName = json.getString("first_name");
            String lastName = json.getString("last_name");
            String email = json.getString("email");
            String password = json.getString("password");
            String userPubKey = json.getString("public_key");

            if(db.userValueExist("username", username)) return errorMessage(ErrorList.DB_USER_EXIST);
            if(db.userValueExist("email", email)) return errorMessage(ErrorList.DB_EMAIL_EXIST);

            if(username.length() < 4 || !username.matches("^[a-zA-Z0-9 ]*$") ||
                    !firstName.matches("^[a-zA-Z0-9 ]*$") || password.length() < 6 || userPubKey.length() < 128)
                correct_data = false;

            if(correct_data)
                if(!db.addNewUser(username, firstName, lastName, email, crypto.sha256(password), userPubKey, userIp))
                    return errorMessage(ErrorList.DB_FAIL);

        } catch (JSONException e) {
            correct_data = false;
        }

        if(!correct_data) return errorMessage(ErrorList.INCORRECT_DATA);

        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("result", "success");
        jsonResponse.put("actionId", ActionList.REGISTRATION);

        return jsonResponse;
    }

    private JSONObject errorMessage(int errorId) {
        JSONObject json = new JSONObject();
        json.put("errorId", errorId);
        json.put("result", "error");
        json.put("actionId", ActionList.ERROR_MESSAGE);

        return json;
    }

}

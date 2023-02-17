package org.mserver.network;

import org.mserver.database.Database;
import org.mserver.database.UserInfo;
import org.mserver.security.Crypto;
import org.mserver.security.TOTP;
import org.mserver.utility.ActionList;
import org.mserver.utility.ErrorList;
import org.mserver.utility.ServerConfig;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Random;

public class RequestHandler {
    private Database db;
    private String userIp, deviceHash;
    private UserInfo userInfo = null;
    private Session userSession;

    private boolean twoFaRequired = false;

    public RequestHandler(String userIp) {
        this.userIp = userIp;
        ServerConfig serverConfig = new ServerConfig();

        db = new Database(serverConfig.getDbUrl(),
                serverConfig.getDbUser(), serverConfig.getDbPassword());
    }
    public String handle(String jsonRequest) {
        JSONObject result = new JSONObject();
        try {
            JSONObject json = new JSONObject(jsonRequest);
            if(deviceHash != null)
            {
                int actionId = json.getInt("aid");
                switch (actionId)
                {
                    case ActionList.REGISTRATION:
                        result = registration(json);
                        break;

                    case ActionList.AUTHORIZATION:
                        result = authorization(json);
                        break;

                    case ActionList.KEY_AUTHORIZATION:
                        result = keyAuthorization(json);
                        break;

                    case ActionList.SET_2FA:
                        result = setTwoFA(json);
                        break;

                    case ActionList.GET_AUSER_DATA:
                        result = getAUserData();
                        break;
                }
            }else {
                deviceHash = json.getString("device_hash");
                if(deviceHash != null) return "1";
            }

        } catch (JSONException e) {
            return errorMessage(ErrorList.INCORRECT_REQUEST_FORM).toString();
        }

        return result.toString();
    }

    private JSONObject registration(JSONObject json) {
        if(authorized()) return errorMessage(ErrorList.INCORRECT_REQUEST_FORM);
        boolean correct_data = true;
        try {
            String username = json.getString("username").toLowerCase();
            String firstName = json.getString("first_name");
            String lastName = json.getString("last_name");
            String email = json.getString("email");
            String password = json.getString("password");
            String userPubKey = json.getString("public_key");

            if(db.isUsernameExist("username")) return errorMessage(ErrorList.DB_USER_EXIST);
            if(db.isEmailExist("email")) return errorMessage(ErrorList.DB_EMAIL_EXIST);

            if(username.length() < 4 || !username.matches("^[a-zA-Z0-9 ]*$") ||
                    !firstName.matches("^[a-zA-Z0-9 ]*$") || password.length() < 6 || userPubKey.length() < 128)
                correct_data = false;

            if(correct_data)
                db.addNewUser(username, firstName, lastName, email, Crypto.sha256(password), userPubKey, userIp);

        } catch (JSONException | SQLException e) {
            correct_data = false;
        }

        if(!correct_data) return errorMessage(ErrorList.INCORRECT_DATA);

        return resultMessage(ActionList.SUCCESS);
    }

    private JSONObject authorization(JSONObject json) {
            try {
                if(userInfo == null) {
                    String login = json.getString("login");
                    String password = json.getString("password");

                    if (!db.userAuth(login, Crypto.sha256(password))) return errorMessage(ErrorList.INCORRECT_DATA);
                    userInfo = db.getUserInfo(login);

                    String twoFaKey = userInfo.getTwoFaKey();

                    if(twoFaKey != null) {
                        twoFaRequired = true;
                        return resultMessage(ActionList.REQUIRED_2FA);
                    }

                    newAuthSession();
                    return resultMessage(ActionList.SUCCESS);

                } else {
                    String twoFaCode = json.getString("2fa_code");

                    if(!new TOTP().verifyCode(userInfo.getTwoFaKey(), twoFaCode))
                        return errorMessage(ErrorList.INCORRECT_DATA);

                    twoFaRequired = false;
                    newAuthSession();
                    return resultMessage(ActionList.SUCCESS);
                }

            } catch (JSONException | SQLException e) {
                return errorMessage(ErrorList.INCORRECT_DATA);
            }
    }

    private JSONObject keyAuthorization(JSONObject json) {
        if(authorized()) return errorMessage(ErrorList.INCORRECT_REQUEST_FORM);
        try {
            String authKey = json.getString("auth_key");
            Session session = db.getSession(authKey);

            if(session != null) {
                if(!initialKeyToSession(session.getInitialKey()).equals(authKey) || !session.getUserIp().equals(userIp))
                    return errorMessage(ErrorList.INCORRECT_DATA);

                userInfo = db.getUserInfo(session.getUserId());
                userSession = session;

                return resultMessage(ActionList.SUCCESS);
            }

        }catch (JSONException | SQLException e) {
            throw new RuntimeException(e);
        }

        return errorMessage(ErrorList.INCORRECT_REQUEST_FORM);
    }

    private JSONObject getAUserData() {
        JSONObject response = new JSONObject();
        response.put("username", userInfo.getUsername());
        response.put("first_name", userInfo.getFirstName());
        response.put("last_name", userInfo.getLastName());
        response.put("email", userInfo.getEmail());
        response.put("aid", ActionList.GET_AUSER_DATA);

        return response;
    }

    private JSONObject setTwoFA(JSONObject json) {
        if(!authorized()) return errorMessage(ErrorList.UNAUTHORIZED_USER);
        try {
            String password = json.getString("password");
            boolean valid = db.userAuth(userInfo.getUsername(), Crypto.sha256(password));
            if(!valid) return errorMessage(ErrorList.INCORRECT_DATA);

            String twoFaKey = userInfo.getTwoFaKey();
            TOTP totp = new TOTP();

            if(twoFaKey != null) {
                String code = json.getString("2fa_code");
                if(!totp.verifyCode(twoFaKey, code))
                    return errorMessage(ErrorList.INCORRECT_DATA);
            }

            db.setTwoFa(userInfo.getId(), totp.generateSecret());
            userInfo = db.getUserInfo(userSession.getUserId());

        } catch (JSONException | SQLException e) {
            return errorMessage(ErrorList.INCORRECT_REQUEST_FORM);
        }

        JSONObject response = new JSONObject();
        response.put("2fa_secret", userInfo.getTwoFaKey());
        response.put("aid", ActionList.SET_2FA);

        return response;
    }

    private void newAuthSession() {
        String initialKey = generateRandomString(12);
        String hash = initialKeyToSession(initialKey);

        try {
            Session session = new Session(userInfo.getId(), initialKey, hash,
                    userIp, new Timestamp(System.currentTimeMillis()));

            db.addNewSession(session);
            userSession = session;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean authorized() {
        return userSession != null || userInfo != null;
    }

    private String initialKeyToSession(String initialKey) {
        return Crypto.sha256(deviceHash + "|" + initialKey);
    }

    private String generateRandomString(int length) {
        final String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(charset.length());
            sb.append(charset.charAt(randomIndex));
        }
        return sb.toString();
    }

    private JSONObject errorMessage(int errorId) {
        JSONObject json = new JSONObject();
        json.put("errorId", errorId);
        json.put("aid", ActionList.ERROR_MESSAGE);

        return json;
    }

    private JSONObject resultMessage(int actionId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("aid", actionId);
        return jsonObject;
    }

}

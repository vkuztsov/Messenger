package org.mserver.network;

import java.sql.Timestamp;

public class Session {
    private int userId;
    private String initialKey, sessionKey, userIp;
    private Timestamp lastVisit;

    public Session(int userId, String initialKey, String sessionKey, String userIp, Timestamp lastVisit) {
        this.userId = userId;
        this.initialKey = initialKey;
        this.sessionKey = sessionKey;
        this.userIp = userIp;
        this.lastVisit = lastVisit;
    }

    public Timestamp getLastVisit() {
        return lastVisit;
    }

    public int getUserId() {
        return userId;
    }

    public String getInitialKey() {
        return initialKey;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public String getUserIp() {
        return userIp;
    }
}

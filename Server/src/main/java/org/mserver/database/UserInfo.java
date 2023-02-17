package org.mserver.database;

public class UserInfo {
    private int id;
    private String username, firstName, lastName, email, photoUrl, publicKey, twoFaKey;

    public UserInfo(int id, String username, String firstName,
                    String lastName, String email, String photoUrl, String publicKey, String twoFaKey) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.photoUrl = photoUrl;
        this.publicKey = publicKey;
        this.twoFaKey = twoFaKey;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getTwoFaKey() {
        return twoFaKey;
    }
}

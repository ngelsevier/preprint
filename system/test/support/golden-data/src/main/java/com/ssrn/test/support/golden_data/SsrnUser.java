package com.ssrn.test.support.golden_data;

public class SsrnUser {

    private final String username;
    private final String publicDisplayName;
    private final String id;
    private String emailAddress;
    private final boolean removed;

    SsrnUser(String id, String username, String publicDisplayName, String emailAddress, boolean removed) {
        this.username = username;
        this.publicDisplayName = publicDisplayName;
        this.id = id;
        this.emailAddress = emailAddress;
        this.removed = removed;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPublicDisplayName() {
        return publicDisplayName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public boolean isRemoved() {
        return removed;
    }
}

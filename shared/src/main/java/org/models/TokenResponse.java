package org.models;

import java.io.Serializable;

public class TokenResponse implements Serializable {

    private final String token;
    private final boolean wasSuccessful;

    public TokenResponse(String token, boolean wasSuccessful) {
        this.token = token;
        this.wasSuccessful = wasSuccessful;
    }

    public String getToken() {
        return token;
    }

    public boolean wasSuccessful() {
        return wasSuccessful;
    }
}

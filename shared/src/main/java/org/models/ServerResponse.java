package org.models;

import java.io.Serializable;

public record ServerResponse<T extends Serializable>(boolean wasAllowed, boolean wasSuccess, T response) implements Serializable {

    public static ServerResponse<Empty> SUCCESS() {
        return new ServerResponse<>(true, true, null);
    }

    public static ServerResponse<Empty> NOT_ALLOWED() {
        return new ServerResponse<>(false, false, null);
    }

    public static <T extends Serializable> ServerResponse<T> NOT_ALLOWED(T response) {
        return new ServerResponse<>(false, false, null);
    }
    public static <T extends Serializable> ServerResponse<T> SUCCESS(T response) {
        return new ServerResponse<>(true, true, response);
    }

    public static ServerResponse<Empty> FAILED() {
        return new ServerResponse<>(true, false, null);
    }
}
package org.models;

import java.io.Serializable;

public record TokenResponse(String token, boolean wasSuccessful) implements Serializable {

}

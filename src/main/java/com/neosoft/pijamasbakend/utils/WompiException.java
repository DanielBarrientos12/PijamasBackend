package com.neosoft.pijamasbakend.utils;

import org.springframework.http.HttpStatusCode;

public class WompiException extends RuntimeException {

    private final HttpStatusCode status;

    public WompiException(HttpStatusCode status, String body) {
        super("Wompi " + status.value() + " → " + body);
        this.status = status;
    }

    public HttpStatusCode getStatus() {
        return status;
    }
}
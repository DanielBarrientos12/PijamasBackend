package com.neosoft.pijamasbakend.models;

public record PaymentSourceRequest(String type, String token,
                                   String acceptance_token, String customer_email) {
}

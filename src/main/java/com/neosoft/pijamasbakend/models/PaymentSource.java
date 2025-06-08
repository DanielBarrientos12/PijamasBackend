package com.neosoft.pijamasbakend.models;

import java.util.Map;

public record PaymentSource(Data data) {
    public record Data(Integer id, String type, String status,
                       String customer_email, Map<String, Object> public_data) {
    }
}
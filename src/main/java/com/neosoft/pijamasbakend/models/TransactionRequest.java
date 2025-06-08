package com.neosoft.pijamasbakend.models;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.net.URI;
import java.util.Map;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record TransactionRequest(
        String acceptance_token,
        long amount_in_cents,
        String currency,
        String signature,
        String customer_email,
        Map<String, Object> payment_method,
        Long payment_source_id,
        URI redirect_url,
        String reference,
        String expiration_time,
        Map<String, Object> customer_data,
        Map<String, Object> shipping_address
) {
}
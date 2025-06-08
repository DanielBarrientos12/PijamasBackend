package com.neosoft.pijamasbakend.models;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

public record TransactionResponse(Data data) {

    public record Data(
            String id,
            Instant created_at,
            long amount_in_cents,
            String status,
            String reference,
            String customer_email,
            String currency,
            String payment_method_type,
            Map<String, Object> payment_method,
            Map<String, Object> shipping_address,
            URI redirect_url,
            String payment_link_id
    ) {
    }
}
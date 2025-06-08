package com.neosoft.pijamasbakend.models;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record CheckoutRequest(
        List<CheckoutItemDTO> items,
        String metodoPago,
        Map<String, Object> metodoPagoDetail,
        String acceptanceToken,
        URI redirectUrl,
        Instant expirationTime,
        Map<String, Object> customerData,
        Map<String, Object> shippingAddress
) {
}

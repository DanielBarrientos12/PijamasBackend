package com.neosoft.pijamasbakend.models;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record CheckoutRequest(
        List<CheckoutItemDTO> items,
        BigDecimal envio,
        String metodoPago,
        Map<String, Object> metodoPagoDetail,
        String acceptanceToken,
        String acceptPersonalAuth
) {
}

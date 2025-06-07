package com.neosoft.pijamasbakend.models;


public record CheckoutResponse(
        Integer facturaId,
        String referencia,
        String wompiPaymentUrl,
        String wompiStatus
) {}

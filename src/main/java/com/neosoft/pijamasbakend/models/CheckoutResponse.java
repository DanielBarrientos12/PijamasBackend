package com.neosoft.pijamasbakend.models;


public record CheckoutResponse(
        String referencia,
        String wompiPaymentUrl,
        String estadoFactura,
        Integer facturaId
) {}

package com.neosoft.pijamasbakend.models;


public record CheckoutResponse(
        String referencia,
        String wompiPaymentUrl
) {}

package com.neosoft.pijamasbakend.models;

public record CardTokenRequest(
        String number,
        String cvc,
        String exp_month,
        String exp_year,
        String card_holder
) {}

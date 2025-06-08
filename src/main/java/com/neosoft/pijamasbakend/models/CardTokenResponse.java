package com.neosoft.pijamasbakend.models;

public record CardTokenResponse(Data data) {
    public record Data(String id, String status) {}
}
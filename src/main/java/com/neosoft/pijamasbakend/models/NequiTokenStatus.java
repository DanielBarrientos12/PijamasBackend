package com.neosoft.pijamasbakend.models;

public record NequiTokenStatus(Data data) {
    public record Data(String id, String status, String phone_number, String name) {}
}
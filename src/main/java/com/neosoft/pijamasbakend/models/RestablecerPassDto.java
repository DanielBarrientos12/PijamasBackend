package com.neosoft.pijamasbakend.models;

import lombok.Data;

@Data
public class RestablecerPassDto {

    private String email;
    private String codigo;
    private String newPassword;

}

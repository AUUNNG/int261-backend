package com.example.auth-service.auth;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class JwtRequestUser {
    private String username;
    private String password;
}

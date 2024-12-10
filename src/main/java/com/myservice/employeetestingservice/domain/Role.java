package com.myservice.employeetestingservice.domain;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    MAIN_ADMIN,
    ADMIN,
    USER;

    @Override
    public String getAuthority(){
        return name();
    }
}

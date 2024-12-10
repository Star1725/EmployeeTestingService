package com.myservice.employeetestingservice.domain;

import org.springframework.security.core.GrantedAuthority;

public enum AccessLevel implements GrantedAuthority {
    LEVEL_1,//top secret(TS)
    LEVEL_2,//secret(S)
    LEVEL_3;//for official use(FOU)

    @Override
    public String getAuthority(){
        return name();
    }
}

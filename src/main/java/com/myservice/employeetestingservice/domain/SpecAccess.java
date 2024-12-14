package com.myservice.employeetestingservice.domain;

import org.springframework.security.core.GrantedAuthority;

public enum SpecAccess implements GrantedAuthority {
    SPEC_ACCESS_1,
    SPEC_ACCESS_2,
    SPEC_ACCESS_3;

    @Override
    public String getAuthority() {
        return "";
    }
}

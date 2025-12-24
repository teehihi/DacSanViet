package com.dacsanviet.model;

/**
 * User Role Enum
 */
public enum Role {
    USER("Khách hàng"),
    STAFF("Nhân viên"),
    ADMIN("Quản trị viên");
    
    private final String displayName;
    
    Role(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

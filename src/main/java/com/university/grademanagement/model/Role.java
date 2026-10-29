package com.university.grademanagement.model;

/**
 * Enumeration representing user access roles in the system.
 */
public enum Role {
    ADMIN("Administrator"),
    FACULTY("Faculty Member"),
    STUDENT("Student");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Role fromString(String roleStr) {
        if (roleStr == null) return null;
        for (Role r : values()) {
            if (r.name().equalsIgnoreCase(roleStr.trim())) {
                return r;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + roleStr);
    }
}

package com.pulse.incident.dto;

public enum IncidentSortField {
    CREATED_AT("createdAt"),
    TITLE("title"),
    SEVERITY("severity"),
    STATUS("status");

    private final String property;

    IncidentSortField(String property) {
        this.property = property;
    }

    public String property() {
        return property;
    }
}

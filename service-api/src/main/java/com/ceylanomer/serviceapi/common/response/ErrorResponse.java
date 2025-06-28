package com.ceylanomer.serviceapi.common.response;

public class ErrorResponse {
    private String code;
    private String description;

    public ErrorResponse() {

    }

    public ErrorResponse(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}

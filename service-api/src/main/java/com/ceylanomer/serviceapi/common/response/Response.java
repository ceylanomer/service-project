package com.ceylanomer.serviceapi.common.response;

public class Response<T> {
    private ErrorResponse error;
    private T data;

    public Response() {
    }

    public Response(ErrorResponse error) {
        this.error = error;
    }

    public Response(T data) {
        this.data = data;
    }

    public ErrorResponse getError() {
        return error;
    }

    public T getData() {
        return data;
    }
}

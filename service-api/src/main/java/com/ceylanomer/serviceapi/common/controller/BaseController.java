package com.ceylanomer.serviceapi.common.controller;

import com.ceylanomer.serviceapi.common.response.DataResponse;
import com.ceylanomer.serviceapi.common.response.ErrorResponse;
import com.ceylanomer.serviceapi.common.response.Response;
import com.ceylanomer.serviceapi.common.response.ResponseBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class BaseController {
    public <T> Response<DataResponse<T>> respond(List<T> items) {
        return ResponseBuilder.build(items);
    }

    public <T> Response<DataResponse<T>> respond(List<T> items, int page, int size, Long totalSize) {
        return ResponseBuilder.build(items, page, size, totalSize);
    }

    public <T> Response<DataResponse<T>> respond(List<T> items, int page, int size, Long totalSize, int totalPage) {
        return ResponseBuilder.build(items, page, size, totalSize, totalPage);
    }

    protected <T> Response<T> respond(T item) {
        return ResponseBuilder.build(item);
    }

    protected Response<ErrorResponse> respond(ErrorResponse errorResponse) {
        return ResponseBuilder.build(errorResponse);
    }


}

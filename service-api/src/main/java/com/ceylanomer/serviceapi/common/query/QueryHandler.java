package com.ceylanomer.serviceapi.common.query;

public interface QueryHandler<Q extends Query, R> {
    R handle(Q query);
}

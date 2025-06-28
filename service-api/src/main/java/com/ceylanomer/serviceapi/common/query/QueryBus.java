package com.ceylanomer.serviceapi.common.query;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

@Component
public class QueryBus {
    private final Map<Class<? extends Query>, QueryHandler> handlers = new HashMap<>();
    private final ApplicationContext applicationContext;

    @Autowired
    public QueryBus(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void registerHandlers() {
        Map<String, QueryHandler> beans = applicationContext.getBeansOfType(QueryHandler.class);
        for (QueryHandler handler : beans.values()) {
            Class<? extends Query> queryClass = (Class<? extends Query>) ((ParameterizedType) handler.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
            handlers.put(queryClass, handler);
        }
    }

    public <Q extends Query, R> R execute(Q query) {
        QueryHandler<Q, R> handler = handlers.get(query.getClass());
        if (handler != null) {
            return handler.handle(query);
        } else {
            throw new IllegalArgumentException("No handler registered for " + query.getClass().getName());
        }
    }
}

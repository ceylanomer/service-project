package com.ceylanomer.serviceapi.common.aggregate;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final ApplicationContext applicationContext;

    public void publish(DomainEvent event) {

        List<String> beanNames = getBeanNamesForType(event.getClass());

        beanNames.forEach(beanName -> {
            DomainEventHandler<DomainEvent> handler = (DomainEventHandler<DomainEvent>) applicationContext.getBean(beanName);
            handler.handle(event);
        });
    }

    private List<String> getBeanNamesForType(Class<? extends DomainEvent> eventClass) {
        if (DomainEvent.class.equals(eventClass)) {
            return new ArrayList<>();
        }
        List<String> beanNames = getBeanNamesForType((Class<? extends DomainEvent>) eventClass.getSuperclass());
        beanNames.addAll(Arrays.asList(applicationContext.getBeanNamesForType(ResolvableType.forClassWithGenerics(DomainEventHandler.class, eventClass))));

        return beanNames;
    }

}

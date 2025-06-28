package com.ceylanomer.serviceapi.common.command;

import com.ceylanomer.serviceapi.common.aggregate.BaseAggregate;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

@Service
public class CommandBus {
    private final Map<Class<? extends Command>, CommandHandler> handlers = new HashMap<>();
    private final ApplicationContext applicationContext;


    @Autowired
    public CommandBus(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void registerHandlers() {
        Map<String, CommandHandler> beans = applicationContext.getBeansOfType(CommandHandler.class);
        for (Object bean : beans.values()) {
            if (bean instanceof CommandHandler) {
                CommandHandler handler = (CommandHandler) bean;
                Class<? extends Command> commandClass = (Class<? extends Command>) ((ParameterizedType) handler.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
                handlers.put(commandClass, handler);
            }
        }
    }

    public <C extends Command, R extends BaseAggregate> void execute(C command) {
        CommandHandler<C, R> handler = handlers.get(command.getClass());
        if (handler != null) {
            handler.process(command);
        } else {
            throw new IllegalArgumentException("No handler registered for " + command.getClass().getName());
        }
    }

    public <C extends Command, R extends BaseAggregate> R executeWithResponse(C command) {
        CommandHandler<C, R> handler = handlers.get(command.getClass());
        if (handler != null) {
            return handler.process(command);
        } else {
            throw new IllegalArgumentException("No handler registered for " + command.getClass().getName());
        }
    }
}

package com.ceylanomer.serviceapi.service.command;

import com.ceylanomer.serviceapi.common.command.Command;
import com.ceylanomer.serviceapi.service.persistence.Resource;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DeleteServiceCommand implements Command {
    private String id;
}

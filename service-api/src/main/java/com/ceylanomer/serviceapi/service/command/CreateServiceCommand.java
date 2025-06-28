package com.ceylanomer.serviceapi.service.command;

import com.ceylanomer.serviceapi.common.command.Command;
import com.ceylanomer.serviceapi.service.persistence.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class CreateServiceCommand implements Command {
    private List<Resource> resources;
}

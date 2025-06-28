package com.ceylanomer.serviceapi.service.query;

import com.ceylanomer.serviceapi.common.query.Query;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetServiceByIdQuery implements Query {
    private String id;

}

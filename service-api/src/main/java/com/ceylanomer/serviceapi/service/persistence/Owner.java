package com.ceylanomer.serviceapi.service.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Owner {
    private String id;
    private String name;
    private String accountNumber;
    private Integer level;
}

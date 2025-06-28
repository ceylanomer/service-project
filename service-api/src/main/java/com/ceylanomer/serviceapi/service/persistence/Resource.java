package com.ceylanomer.serviceapi.service.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resource {
    private String id;
    private List<Owner> owners;
}

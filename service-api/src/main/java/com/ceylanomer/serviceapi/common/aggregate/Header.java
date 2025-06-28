package com.ceylanomer.serviceapi.common.aggregate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Header {
    private Date timeStamp;
    private Long version;
    private String messageId;

    public Header(Long version) {
        this.timeStamp = Date.from(LocalDateTime.now().toLocalDate().atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant());
        this.version = version;
        this.messageId = java.util.UUID.randomUUID().toString();
    }
}

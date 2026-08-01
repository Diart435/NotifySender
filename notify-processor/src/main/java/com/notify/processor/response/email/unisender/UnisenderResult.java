package com.notify.processor.response.email.unisender;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnisenderResult {
    @JsonProperty("email_id")
    private Integer emailId;
}

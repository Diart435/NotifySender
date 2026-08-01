package com.notify.processor.response.email.unisender;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnisenderResponse {
    private UnisenderResult result;
}

package com.example.smspricing.controller.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SMSRequest {
    String from;
    String body;
    String to;
    Long customerId;
}

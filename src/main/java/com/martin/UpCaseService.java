package com.martin;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

import java.util.Collection;

@MessagingGateway
public interface UpCaseService {
    @Gateway(requestChannel = "upcase.input")
    Collection<String> upCaseStrings(Collection<String>strings);
}

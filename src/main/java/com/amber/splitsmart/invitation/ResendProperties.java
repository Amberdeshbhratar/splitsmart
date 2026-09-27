package com.amber.splitsmart.invitation;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.resend")
public record ResendProperties(String apiKey, String from) { }

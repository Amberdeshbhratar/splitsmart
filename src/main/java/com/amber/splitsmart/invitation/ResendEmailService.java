package com.amber.splitsmart.invitation;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ResendEmailService {
    private final ResendProperties properties;
    ResendEmailService(ResendProperties properties) { this.properties = properties; }

    public void send(String to, String subject, String text) {
        if (!StringUtils.hasText(properties.apiKey()) || !StringUtils.hasText(properties.from()))
            throw new IllegalStateException("Resend requires RESEND_API_KEY and RESEND_FROM environment variables");
        try {
            Resend resend = new Resend(properties.apiKey());
            resend.emails().send(CreateEmailOptions.builder().from(properties.from()).to(to).subject(subject).text(text).build());
        } catch (ResendException error) {
            throw new IllegalStateException("Resend rejected the email: " + error.getMessage(), error);
        }
    }
}

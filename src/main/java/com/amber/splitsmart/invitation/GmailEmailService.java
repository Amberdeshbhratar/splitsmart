package com.amber.splitsmart.invitation;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class GmailEmailService {
    private final JavaMailSender sender;
    GmailEmailService(ObjectProvider<JavaMailSender> sender) { this.sender = sender.getIfAvailable(); }

    public void send(String from, String to, String subject, String text) {
        if (sender == null || !StringUtils.hasText(from)) throw new IllegalStateException("Gmail requires SPRING_MAIL_* settings and MAIL_FROM");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        sender.send(message);
    }
}

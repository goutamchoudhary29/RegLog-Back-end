package com.reglog.backend.service;

import com.reglog.backend.config.AppProperties;
import com.reglog.backend.exception.EmailDeliveryException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final AppProperties properties;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender,
                        AppProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    public void sendOtp(String to, String otp) {
        if (mailSender == null) {
            throw new EmailDeliveryException("Email service is not configured");
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(properties.mail.getFrom());
            helper.setTo(to);
            helper.setSubject("Your password reset OTP");
            helper.setText("""
                    <div style="font-family:Arial,sans-serif">
                      <h2>Password reset verification</h2>
                      <p>Your one-time password is:</p>
                      <p style="font-size:28px;font-weight:bold;letter-spacing:6px">%s</p>
                      <p>This OTP expires in 5 minutes. Do not share it with anyone.</p>
                    </div>
                    """.formatted(otp), true);
            mailSender.send(message);
        } catch (Exception ex) {
            throw new EmailDeliveryException("Unable to send OTP email");
        }
    }
}

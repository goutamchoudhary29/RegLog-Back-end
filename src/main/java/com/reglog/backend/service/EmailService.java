package com.reglog.backend.service;

import com.reglog.backend.config.AppProperties;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final AppProperties properties;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender,
                        AppProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    public void sendOtp(String to, String otp) {
        log.info("==================================================");
        log.info("[PASSWORD RESET OTP] Target email: {}", to);
        log.info("[PASSWORD RESET OTP] Generated OTP: {}", otp);
        log.info("[PASSWORD RESET OTP] Validity: 5 minutes");
        log.info("==================================================");

        if (mailSender == null) {
            log.info("MailSender bean is not configured; logged OTP to console for local testing.");
            return;
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
            log.info("Successfully dispatched OTP email via SMTP to {}", to);
        } catch (Exception ex) {
            log.warn("Could not dispatch email via SMTP ({}). OTP was logged above for verification.", ex.getMessage());
        }
    }
}

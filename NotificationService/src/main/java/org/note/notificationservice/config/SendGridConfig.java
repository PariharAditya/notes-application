package org.note.notificationservice.config;

import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SendGridConfig {

    @Bean
    public SendGrid sendGrid() {
        String sendgridApiKey = "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";
        return new SendGrid(sendgridApiKey);
    }
}

package org.note.notesapplication.Config;

import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SendGridConfig {

    @Value("${sendgrid.api.key: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}")
    private String sendgridApiKey;

    @Bean
    public SendGrid sendGrid() {
        return new SendGrid(sendgridApiKey);
    }
}
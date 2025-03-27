package org.note.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    @Bean
    public Queue emailQueue() {
        return new Queue("Email-notification", true);
    }

    @Bean
    public Queue emailWithDocsQueue() {
        return new Queue("Email-notification-with-docs", true);
    }

    @Bean
    public Queue smsQueue() {
        return new Queue("SMS-notification", true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange("notification-exchange");
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue()).to(exchange()).with("notification.email");
    }

    @Bean
    public Binding emailWithDocsBinding() {
        return BindingBuilder.bind(emailWithDocsQueue()).to(exchange()).with("notification.email.docs");
    }

    @Bean
    public Binding smsBinding() {
        return BindingBuilder.bind(smsQueue()).to(exchange()).with("notification.sms");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
package org.note.apigateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GatewayConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        // Increase memory buffer size for handling large request/response bodies
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
        
        return WebClient.builder()
                .exchangeStrategies(exchangeStrategies);
    }
    
    // Configure the existing serverCodecConfigurer instead of creating a new bean
    @Autowired
    public void configureServerCodecConfigurer(ServerCodecConfigurer configurer) {
        // Increase codec memory limits to avoid "Exceeded limit on max bytes to buffer" errors
        configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024);
    }
}

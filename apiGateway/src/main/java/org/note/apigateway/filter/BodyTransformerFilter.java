package org.note.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class BodyTransformerFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Only process specific routes/paths
        String path = request.getURI().getPath();
        if (!path.contains("/notes")) {
            return chain.filter(exchange);
        }
        
        // Only process JSON requests with a body
        if (!isJsonRequest(request)) {
            return chain.filter(exchange);
        }
        
        return DataBufferUtils.join(request.getBody())
            .flatMap(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                String bodyStr = new String(bytes, StandardCharsets.UTF_8);
                
                log.info("Original request body: {}", bodyStr);
                
                // If the body looks malformed, try to fix it
                // Here we're just logging it, but could transform it if needed
                
                // Create a new request with the original body for now
                DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
                DataBuffer buffer = bufferFactory.wrap(bytes);
                
                ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(request) {
                    @Override
                    public Flux<DataBuffer> getBody() {
                        return Flux.just(buffer);
                    }
                };
                
                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            });
    }
    
    private boolean isJsonRequest(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        MediaType contentType = headers.getContentType();
        return contentType != null && contentType.includes(MediaType.APPLICATION_JSON);
    }

    @Override
    public int getOrder() {
        // Execute after LoggingFilter but before routing
        return 0;
    }
}

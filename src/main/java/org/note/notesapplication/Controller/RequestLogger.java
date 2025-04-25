package org.note.notesapplication.Controller;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class RequestLogger extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        
        try {
            // Log request details before processing
            log.info("Request method: {}, URI: {}", request.getMethod(), request.getRequestURI());
            log.info("Request headers: ");
            request.getHeaderNames().asIterator().forEachRemaining(headerName -> 
                log.info("{}: {}", headerName, request.getHeader(headerName)));
            
            // Process the request
            filterChain.doFilter(requestWrapper, responseWrapper);
            
            // Log request body after processing (when it's available)
            String requestBody = new String(requestWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
            if (!requestBody.isEmpty()) {
                log.info("Request body: {}", requestBody);
            }
            
            // Log response details
            int status = responseWrapper.getStatus();
            log.info("Response status: {}", status);
            
            // Log response body if there was an error
            if (status >= 400) {
                String responseBody = new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
                log.info("Response body: {}", responseBody);
            }
        } finally {
            // This is important: copy content of response wrapper back into original response
            responseWrapper.copyBodyToResponse();
        }
    }
}

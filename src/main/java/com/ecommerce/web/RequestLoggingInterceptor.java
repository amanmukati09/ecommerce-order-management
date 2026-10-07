package com.ecommerce.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Logs each incoming API request with method, URI, and processing time.
 */
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
    private static final String START_TIME = "requestStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME, System.currentTimeMillis());
        logger.debug("--> {} {}{}", request.getMethod(), request.getRequestURI(),
                request.getQueryString() != null ? "?" + request.getQueryString() : "");
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Object start = request.getAttribute(START_TIME);
        long elapsed = start != null ? System.currentTimeMillis() - (Long) start : -1;
        logger.debug("<-- {} {} [{}] {}ms", request.getMethod(), request.getRequestURI(),
                response.getStatus(), elapsed);
    }
}

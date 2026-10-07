package com.ecommerce.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Standard API response envelope used across controllers.
 */
public class ApiResponse {

    private boolean success;
    private String message;
    private Object data;
    private Map<String, Object> meta;

    public ApiResponse() {
        this.meta = new HashMap<>();
    }

    public ApiResponse(boolean success, String message) {
        this();
        this.success = success;
        this.message = message;
    }

    public static ApiResponse ok(String message) {
        return new ApiResponse(true, message);
    }

    public static ApiResponse ok(String message, Object data) {
        ApiResponse r = new ApiResponse(true, message);
        r.setData(data);
        return r;
    }

    public static ApiResponse error(String message) {
        return new ApiResponse(false, message);
    }

    public ApiResponse withMeta(String key, Object value) {
        this.meta.put(key, value);
        return this;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    public Map<String, Object> getMeta() { return meta; }
    public void setMeta(Map<String, Object> meta) { this.meta = meta; }
}

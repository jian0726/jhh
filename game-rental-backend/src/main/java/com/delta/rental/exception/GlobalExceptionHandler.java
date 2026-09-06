package com.delta.rental.exception;

import com.delta.rental.dto.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Object handleBusinessException(BusinessException e) {
        if (isSwaggerRequest()) {
            // 让 Swagger 自己处理
            return null;
        }
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e) {
        if (isSwaggerRequest()) {
            // 让 Swagger 自己处理
            return null;
        }
        return ApiResponse.error(500, "服务器内部错误: " + e.getMessage());
    }

    private boolean isSwaggerRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                String uri = attributes.getRequest().getRequestURI();
                return uri.contains("/v3/api-docs")
                        || uri.contains("/swagger-ui")
                        || uri.contains("/api-docs");
            }
        } catch (Exception ignored) {}
        return false;
    }
}
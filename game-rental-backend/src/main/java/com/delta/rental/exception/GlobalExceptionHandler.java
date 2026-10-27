package com.delta.rental.exception;

import com.delta.rental.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Object handleBusinessException(BusinessException e) {
        if (isSwaggerRequest()) {
            // 让 Swagger 自己处理
            return null;
        }
        return buildResponse(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleValidationException(MethodArgumentNotValidException e) {
        if (isSwaggerRequest()) {
            return null;
        }
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse("参数校验失败");
        return buildResponse(400, message);
    }

    @ExceptionHandler(org.springframework.web.bind.MissingRequestHeaderException.class)
    public Object handleMissingHeader(org.springframework.web.bind.MissingRequestHeaderException e) {
        if (isSwaggerRequest()) {
            return null;
        }
        if ("Authorization".equalsIgnoreCase(e.getHeaderName())) {
            return buildResponse(401, "请先登录后再操作");
        }
        return buildResponse(400, "缺少必要的请求头：" + e.getHeaderName());
    }

    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public Object handleMissingParam(org.springframework.web.bind.MissingServletRequestParameterException e) {
        if (isSwaggerRequest()) {
            return null;
        }
        return buildResponse(400, "缺少必要的参数：" + e.getParameterName());
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public Object handleMethodNotSupported(org.springframework.web.HttpRequestMethodNotSupportedException e) {
        if (isSwaggerRequest()) {
            return null;
        }
        return buildResponse(405, "请求方式不支持：" + e.getMethod());
    }

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e) {
        if (isSwaggerRequest()) {
            // 让 Swagger 自己处理
            return null;
        }
        return buildResponse(500, "服务器内部错误: " + e.getMessage());
    }

    /**
     * 统一错误响应：
     * - HTTP 状态码使用真实业务码，便于前端按语义处理（且 axios 会走 reject 分支）
     * - body 里同时把 message 放进 data，兼容前端直接读取 data.message 的写法
     */
    private ResponseEntity<ApiResponse<Map<String, Object>>> buildResponse(Integer code, String message) {
        int status = (code != null && code >= 100 && code <= 599) ? code : 500;
        Map<String, Object> data = new HashMap<>();
        data.put("success", false);
        data.put("message", message);
        ApiResponse<Map<String, Object>> body = new ApiResponse<>(status, message, data);
        return ResponseEntity.status(HttpStatus.valueOf(status)).body(body);
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

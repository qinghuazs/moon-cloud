package com.mooncloud.shorturl.exception;

import com.mooncloud.shorturl.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 *
 * @author mooncloud
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理短链系统异常
     */
    @ExceptionHandler(ShortUrlException.class)
    public ResponseEntity<ApiResponse<Object>> handleShortUrlException(ShortUrlException e, HttpServletRequest request) {
        log.warn("短链系统异常: {} - {}", request.getRequestURI(), e.getMessage());

        ApiResponse<Object> response = ApiResponse.error(e.getCode(), e.getMessage());
        return ResponseEntity.status(e.getCode()).body(response);
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResponse<Object>> handleValidationException(Exception e, HttpServletRequest request) {
        log.warn("参数验证异常: {} - {}", request.getRequestURI(), e.getMessage());

        Map<String, String> errors = new HashMap<>();

        if (e instanceof MethodArgumentNotValidException validationException) {
            validationException.getBindingResult().getAllErrors().forEach(error -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            });
        } else if (e instanceof BindException bindException) {
            bindException.getBindingResult().getAllErrors().forEach(error -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            });
        }

        ApiResponse<Object> response = ApiResponse.badRequest("参数验证失败");
        response.setData(errors);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("非法参数异常: {} - {}", request.getRequestURI(), e.getMessage());

        ApiResponse<Object> response = ApiResponse.badRequest(e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception e, HttpServletRequest request) {
        log.error("系统异常: {} - {}", request.getRequestURI(), e.getMessage(), e);

        ApiResponse<Object> response = ApiResponse.error("系统内部错误，请稍后重试");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
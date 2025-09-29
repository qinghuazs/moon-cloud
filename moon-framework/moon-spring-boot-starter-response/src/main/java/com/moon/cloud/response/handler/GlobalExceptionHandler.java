package com.moon.cloud.response.handler;

import com.moon.cloud.response.enums.ResponseCode;
import com.moon.cloud.response.web.MoonCloudResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.nio.file.AccessDeniedException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理所有异常，返回标准响应格式
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public MoonCloudResponse<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("业务异常 [{}] {}: {}", request.getRequestURI(), e.getCode(), e.getMessage());
        return MoonCloudResponse.error(e.getCode(), e.getMessage())
                .withTraceId(getTraceId(request));
    }

    /**
     * 处理参数校验异常 - @RequestBody 参数校验失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MoonCloudResponse<?> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String message = "参数校验失败: " + errors.entrySet().stream()
                .map(entry -> entry.getKey() + " - " + entry.getValue())
                .collect(Collectors.joining("; "));

        log.warn("参数校验失败 [{}]: {}", request.getRequestURI(), message);
        return MoonCloudResponse.error(ResponseCode.PARAM_ERROR, message)
                .withExtra(errors)
                .withTraceId(getTraceId(request));
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MoonCloudResponse<?> handleBindException(BindException e, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String message = "参数绑定失败: " + errors.entrySet().stream()
                .map(entry -> entry.getKey() + " - " + entry.getValue())
                .collect(Collectors.joining("; "));

        log.warn("参数绑定失败 [{}]: {}", request.getRequestURI(), message);
        return MoonCloudResponse.error(ResponseCode.PARAM_ERROR, message)
                .withExtra(errors)
                .withTraceId(getTraceId(request));
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MoonCloudResponse<?> handleConstraintViolationException(
            ConstraintViolationException e, HttpServletRequest request) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String message = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));

        log.warn("约束违反 [{}]: {}", request.getRequestURI(), message);
        return MoonCloudResponse.error(ResponseCode.PARAM_ERROR, "参数约束违反: " + message)
                .withTraceId(getTraceId(request));
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MoonCloudResponse<?> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e, HttpServletRequest request) {
        String message = String.format("缺少必需的请求参数: %s", e.getParameterName());
        log.warn("缺少请求参数 [{}]: {}", request.getRequestURI(), message);
        return MoonCloudResponse.error(ResponseCode.PARAM_MISSING, message)
                .withTraceId(getTraceId(request));
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MoonCloudResponse<?> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String message = String.format("参数类型不匹配: %s 应该是 %s 类型",
                e.getName(), e.getRequiredType().getSimpleName());
        log.warn("参数类型不匹配 [{}]: {}", request.getRequestURI(), message);
        return MoonCloudResponse.error(ResponseCode.PARAM_TYPE_ERROR, message)
                .withTraceId(getTraceId(request));
    }

    /**
     * 处理消息不可读异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MoonCloudResponse<?> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn("请求体解析失败 [{}]: {}", request.getRequestURI(), e.getMessage());
        return MoonCloudResponse.error(ResponseCode.PARAM_FORMAT_ERROR, "请求体格式错误")
                .withTraceId(getTraceId(request));
    }

    /**
     * 处理请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public MoonCloudResponse<?> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        String message = String.format("不支持 %s 请求方法", e.getMethod());
        log.warn("请求方法不支持 [{}]: {}", request.getRequestURI(), message);
        return MoonCloudResponse.error(ResponseCode.METHOD_NOT_ALLOWED, message)
                .withTraceId(getTraceId(request));
    }

    /**
     * 处理媒体类型不支持异常
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public MoonCloudResponse<?> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        log.warn("媒体类型不支持 [{}]: {}", request.getRequestURI(), e.getMessage());
        return MoonCloudResponse.error(ResponseCode.UNSUPPORTED_MEDIA_TYPE, "不支持的媒体类型")
                .withTraceId(getTraceId(request));
    }

    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public MoonCloudResponse<?> handleNoHandlerFoundException(
            NoHandlerFoundException e, HttpServletRequest request) {
        String message = String.format("找不到资源: %s", e.getRequestURL());
        log.warn("404 [{}]: {}", request.getRequestURI(), message);
        return MoonCloudResponse.error(ResponseCode.NOT_FOUND, message)
                .withTraceId(getTraceId(request));
    }

    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public MoonCloudResponse<?> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e, HttpServletRequest request) {
        log.warn("文件大小超限 [{}]: {}", request.getRequestURI(), e.getMessage());
        return MoonCloudResponse.error(ResponseCode.FILE_SIZE_EXCEED, "文件大小超过限制")
                .withTraceId(getTraceId(request));
    }

    /**
     * 处理访问拒绝异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public MoonCloudResponse<?> handleAccessDeniedException(
            AccessDeniedException e, HttpServletRequest request) {
        log.error("访问被拒绝 [{}]: {}", request.getRequestURI(), e.getMessage());
        return MoonCloudResponse.error(ResponseCode.FORBIDDEN, "访问被拒绝")
                .withTraceId(getTraceId(request));
    }

    /**
     * 处理数据库相关异常
     */
    @ExceptionHandler(SQLException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public MoonCloudResponse<?> handleSQLException(SQLException e, HttpServletRequest request) {
        log.error("数据库异常 [{}]: ", request.getRequestURI(), e);
        return MoonCloudResponse.error(ResponseCode.DATABASE_ERROR, "数据库操作失败")
                .withTraceId(getTraceId(request));
    }



    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public MoonCloudResponse<?> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常 [{}]: ", request.getRequestURI(), e);
        return MoonCloudResponse.error(ResponseCode.SYSTEM_ERROR, "系统运行异常")
                .withTraceId(getTraceId(request));
    }

    /**
     * 处理所有其他异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public MoonCloudResponse<?> handleException(Exception e, HttpServletRequest request) {
        log.error("未知异常 [{}]: ", request.getRequestURI(), e);
        return MoonCloudResponse.error(ResponseCode.INTERNAL_SERVER_ERROR, "服务器内部错误")
                .withTraceId(getTraceId(request));
    }

    /**
     * 获取追踪ID
     * 优先从请求头获取，如果没有则生成一个
     */
    private String getTraceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = request.getHeader("X-Request-Id");
        }
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = java.util.UUID.randomUUID().toString();
        }
        return traceId;
    }
}
package org.paland.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import lombok.extern.slf4j.Slf4j;
import org.paland.common.result.Result;
import org.paland.common.result.ResultCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        ResultCode resultCode = e.getResultCode();
        log.warn("业务异常: code={}, message={}", resultCode.getCode(), e.getMessage());

        Result<Void> result = Result.fail(resultCode, e.getMessage());
        return ResponseEntity.status(resultCode.getHttpStatus()).body(result);
    }

    /**
     * 请求参数校验失败（如 @Valid 配合 @NotNull/@NotBlank 等注解触发）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null
                ? fieldError.getField() + " " + fieldError.getDefaultMessage()
                : ResultCode.PARAM_ERROR.getMessage();

        log.warn("参数校验失败: {}", message);

        Result<Void> result = Result.fail(ResultCode.PARAM_ERROR, message);
        return ResponseEntity.status(ResultCode.PARAM_ERROR.getHttpStatus()).body(result);
    }

    /**
     * 访问了不存在的接口路径
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Result<Void>> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("接口不存在: {} {}", e.getHttpMethod(), e.getRequestURL());

        Result<Void> result = Result.fail(ResultCode.RESOURCE_NOT_FOUND);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }

    /**
     * 静态资源未找到（如浏览器自动请求 favicon.ico）
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("资源未找到: {}", e.getMessage());

        Result<Void> result = Result.fail(ResultCode.RESOURCE_NOT_FOUND);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }

    /**
     * 请求方法不被支持（如接口只允许 POST，却用 GET 访问）
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持: {}", e.getMessage());

        Result<Void> result = Result.fail(ResultCode.METHOD_NOT_ALLOWED);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(result);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("系统未捕获异常", e);

        Result<Void> result = Result.fail(ResultCode.SYSTEM_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    // 拦截 Sa-Token 全局未登录异常
    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handlerNotLoginException(NotLoginException nle) {
        // 打印堆栈或日志（可选）
        // 根据不同场景，nle.getType() 可以拿到具体是 Token过期、未提供Token 还是 异地登录被踢
        return Result.fail(ResultCode.UNAUTHORIZED, "令牌无效或已过期，请重新登录");
    }
}
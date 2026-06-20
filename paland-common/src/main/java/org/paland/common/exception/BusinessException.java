package org.paland.common.exception;

import lombok.Getter;
import org.paland.common.result.ResultCode;

/**
 * 业务异常
 * <p>
 * 用于在 Service 层主动抛出"预期内"的业务错误（如参数校验失败、资源不存在等），
 * 由全局异常处理器统一捕获并转换成规范的 Result 响应，避免每个 Controller 方法
 * 都手写 try-catch。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ResultCode resultCode;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    /**
     * 自定义提示信息，覆盖 ResultCode 默认的 message
     * （例如 USER_NOT_FOUND 但想提示"用户ID=123 不存在"）
     */
    public BusinessException(ResultCode resultCode, String customMessage) {
        super(customMessage);
        this.resultCode = resultCode;
    }
}
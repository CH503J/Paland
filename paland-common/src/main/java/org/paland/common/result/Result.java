package org.paland.common.result;

import lombok.Getter;

import java.io.Serializable;

/**
 * 统一返回结果封装类
 * <p>
 * HTTP 状态码由 Controller 层通过 ResponseEntity 设置（反映请求处理的真实结果），
 * 本类只携带业务状态码（code）、提示信息（message）和数据（data），
 * code 用于让调用方精确识别具体业务场景。
 *
 * @param <T> 返回数据的类型
 */
@Getter
public class Result<T> implements Serializable {

    /** 业务状态码，对应 {@link ResultCode#getCode()} */
    private final String code;

    /** 提示信息，默认取 ResultCode 的 message，也可在构造时自定义覆盖 */
    private final String message;

    /** 业务数据 */
    private final T data;

    private Result(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功，无数据
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 成功，带数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 失败，使用预定义的 ResultCode
     */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 失败，使用预定义的 ResultCode，但自定义提示信息
     * （比如同样是 PARAM_ERROR，想提示具体是哪个参数错了）
     */
    public static <T> Result<T> fail(ResultCode resultCode, String customMessage) {
        return new Result<>(resultCode.getCode(), customMessage, null);
    }
}
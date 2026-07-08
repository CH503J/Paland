package org.paland.common.result;

import lombok.Getter;

/**
 * 业务状态码枚举
 * <p>
 * 与 HTTP 状态码配合使用：每个业务状态码自带对应的 HTTP 状态码语义，
 * 全局异常处理器据此决定响应的 HTTP 状态，避免维护额外的映射表。
 */
@Getter
public enum ResultCode {

    // ========== 通用成功 ==========
    SUCCESS("SUCCESS", "操作成功", 200),

    // ========== 通用失败（4xx 语义） ==========
    PARAM_ERROR("PARAM_ERROR", "请求参数错误", 400),
    UNAUTHORIZED("UNAUTHORIZED", "未登录或登录已过期", 401),
    FORBIDDEN("FORBIDDEN", "没有权限访问该资源", 403),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "请求的资源不存在", 404),
    METHOD_NOT_ALLOWED("METHOD_NOT_ALLOWED", "请求方法不被支持", 405),

    // ========== 通用失败（5xx 语义） ==========
    SYSTEM_ERROR("SYSTEM_ERROR", "系统内部错误", 500),

    // ========== 用户模块 ==========
    USER_NOT_FOUND("USER_NOT_FOUND", "用户不存在", 404),
    USER_ALREADY_EXISTS("USER_ALREADY_EXISTS", "用户已存在", 409),

    // ========== 定时任务模块 ==========
    INVALID_CRON_EXPRESSION("INVALID_CRON_EXPRESSION", "Cron表达式非法", 400),
    JOB_STATUS_INVALID("JOB_STATUS_INVALID", "定时任务状态无效", 400),
    JOB_NOT_FOUND("JOB_NOT_FOUND", "定时任务不存在", 404),
    JOB_IS_RUNNING("JOB_IS_RUNNING", "定时任务正在运行中", 409),

    // 修复：将原先一堆 500 的动作失败聚合成一个，保持枚举类的清爽
    JOB_OPERATION_FAILED("JOB_OPERATION_FAILED", "定时任务操作失败", 500),
    JOB_EXECUTION_FAILED("JOB_EXECUTION_FAILED", "任务执行失败", 500);

    private final String code;
    private final String message;
    private final int httpStatus;

    ResultCode(String code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
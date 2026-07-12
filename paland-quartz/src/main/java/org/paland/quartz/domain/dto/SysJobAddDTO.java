package org.paland.quartz.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 新增定时任务请求对象。
 *
 * <p>用于接收前端创建定时任务时提交的数据，
 * 仅包含创建任务所需的参数，不涉及数据库字段及运行状态等信息。</p>
 *
 * @author ChenJun
 */
@Data
public class SysJobAddDTO {

    /**
     * 任务名称。
     */
    @NotBlank(message = "任务名称不能为空")
    private String jobName;

    /**
     * 任务分组。
     * <p>默认为 DEFAULT。</p>
     */
    private String jobGroup = "DEFAULT";

    /**
     * 调用目标。
     * <p>例如：palandTask.runNoParams()</p>
     */
    @NotBlank(message = "调用目标不能为空")
    private String invokeTarget;

    /**
     * Cron 表达式。
     */
    @NotBlank(message = "Cron表达式不能为空")
    private String cronExpression;

    /**
     * 错失执行策略。
     */
    private Integer misfirePolicy = 0;

    /**
     * 是否允许并发执行。
     * <p>0：允许；1：禁止。</p>
     */
    private Integer concurrent = 1;
}
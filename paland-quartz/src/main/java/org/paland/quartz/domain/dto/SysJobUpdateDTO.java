package org.paland.quartz.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改定时任务请求对象。
 *
 * <p>用于接收前端修改定时任务时提交的数据，
 * 仅包含修改任务所需的参数，不涉及数据库字段及运行状态等信息。</p>
 *
 * @author ChenJun
 */
@Data
public class SysJobUpdateDTO {

    /**
     * 任务ID。
     */
    @NotBlank(message = "任务ID不能为空")
    private String id;

    /**
     * 任务名称。
     */
    private String jobName;

    /**
     * 调用目标。
     * <p>例如：palandTask.runNoParams()</p>
     */
    private String invokeTarget;

    /**
     * Cron 表达式。
     */
    private String cronExpression;

    /**
     * 错失执行策略。
     */
    private Integer misfirePolicy;

    /**
     * 是否允许并发执行。
     * <p>0：允许；1：禁止。</p>
     */
    private Integer concurrent;

    /**
     * 任务描述。
     */
    private String remark;
}

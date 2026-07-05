package org.paland.system.module.job.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SysJobCreateRequestDTO {

    @NotBlank(message = "任务名不能为空")
    private String jobName;

    private String jobGroup = "DEFAULT";

    @NotBlank(message = "调用目标不能为空")
    private String invokeTarget;

    @NotBlank(message = "cron表达式不能为空")
    private String cronExpression;

    private String remark;
}
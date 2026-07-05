package org.paland.system.module.job.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SysJobUpdateRequestDTO {

    @NotNull(message = "任务ID不能为空")
    private Long id;

    @NotBlank(message = "任务名称不能为空")
    private String jobName;

    private String remark;
}
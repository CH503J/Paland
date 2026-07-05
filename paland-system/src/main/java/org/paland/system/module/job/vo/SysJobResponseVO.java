package org.paland.system.module.job.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysJobResponseVO {

    private Long id;

    private String jobName;

    private String jobGroup;

    private String invokeTarget;

    private String cronExpression;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
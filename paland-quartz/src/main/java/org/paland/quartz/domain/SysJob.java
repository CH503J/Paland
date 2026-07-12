package org.paland.quartz.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;

/**
 * 定时任务实体。
 *
 * <p>对应数据库表 sys_job，用于保存定时任务的业务配置。
 * 该实体记录任务的基本信息及运行配置，是业务层管理定时任务的核心对象，
 * 不直接对应 Quartz 的内部表（QRTZ_*）。</p>
 *
 * @author ChenJun
 */
@Data
@TableName("sys_job")
public class SysJob implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务主键。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 任务名称。
     */
    private String jobName;

    /**
     * 任务分组。
     */
    private String jobGroup;

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
     * <p>0：默认；1：立即触发；2：触发一次；3：不触发。</p>
     */
    private Integer misfirePolicy = 0;

    /**
     * 是否允许并发执行。
     * <p>0：允许；1：禁止。</p>
     */
    private Integer concurrent = 1;

    /**
     * 任务状态。
     * <p>0：正常；1：暂停。</p>
     */
    private Integer status = 0;

    private String remark;
}
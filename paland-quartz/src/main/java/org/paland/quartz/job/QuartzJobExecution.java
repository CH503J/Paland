package org.paland.quartz.job;

import org.paland.quartz.domain.SysJob;
import org.paland.quartz.service.ISysJobService;
import org.paland.quartz.util.AbstractQuartzJob;
import org.paland.quartz.util.JobInvokeUtil;
import org.paland.quartz.util.ScheduleUtils;
import org.paland.quartz.util.SpringUtils;
import org.quartz.JobExecutionContext;


/**
 * 并发执行任务。
 *
 * <p>当定时任务配置为"允许并发"时，Quartz 会使用该 Job 执行任务。
 * 与 {@code QuartzDisallowConcurrentExecution} 不同，本类未使用
 * {@code @DisallowConcurrentExecution} 注解，因此同一个任务允许同时存在多个执行实例。</p>
 *
 * <p>任务执行时，会根据 Quartz 保存的任务 ID 查询最新的业务配置，
 * 再通过反射调用目标方法，避免直接使用 Quartz 中缓存的任务信息。</p>
 *
 * @author ChenJun
 */
public class QuartzJobExecution extends AbstractQuartzJob {

    /**
     * 执行定时任务。
     */
    @Override
    protected void doExecute(JobExecutionContext context) throws Exception {
        // 从 Quartz 上下文中获取任务 ID
        String jobId = (String) context.getMergedJobDataMap().get(ScheduleUtils.TASK_ID_KEY);

        // 从 Spring 容器获取业务服务，并根据任务 ID 查询最新的任务配置。
        // Quartz 仅保存任务 ID，不缓存完整业务对象，避免使用过期数据。
        ISysJobService jobService = SpringUtils.getBean(ISysJobService.class);
        SysJob scheduleJob = jobService.getById(jobId);

        if (scheduleJob != null) {
            JobInvokeUtil.invokeMethod(scheduleJob);
        }
    }
}
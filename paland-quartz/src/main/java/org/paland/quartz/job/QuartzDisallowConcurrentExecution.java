package org.paland.quartz.job;

import org.paland.quartz.domain.SysJob;
import org.paland.quartz.service.ISysJobService;
import org.paland.quartz.util.AbstractQuartzJob;
import org.paland.quartz.util.JobInvokeUtil;
import org.paland.quartz.util.ScheduleUtils;
import org.paland.quartz.util.SpringUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;


/**
 * 非并发执行任务。
 *
 * <p>当定时任务配置为"禁止并发"时，Quartz 会使用该 Job 执行任务，
 * 并通过 {@link DisallowConcurrentExecution} 保证同一个 JobDetail
 * 在上一次执行结束之前不会再次开始执行。</p>
 *
 * <p>任务执行时，会根据 Quartz 保存的任务 ID 查询最新的业务配置，
 * 再通过反射调用目标方法，避免直接使用 Quartz 中缓存的任务信息。</p>
 *
 * @author ChenJun
 */
@DisallowConcurrentExecution
public class QuartzDisallowConcurrentExecution extends AbstractQuartzJob {

    /**
     * 执行定时任务。
     */
    @Override
    protected void doExecute(JobExecutionContext context) throws Exception {
        String jobId = (String) context.getMergedJobDataMap().get(ScheduleUtils.TASK_ID_KEY);

        ISysJobService jobService = SpringUtils.getBean(ISysJobService.class);
        SysJob scheduleJob = jobService.getById(jobId);

        if (scheduleJob != null) {
            // 每次执行前都重新读取数据库中的任务配置，避免使用 Quartz 缓存的旧数据。
            // 即使任务是通过手动触发（runOnce）等方式执行，也能保证使用最新配置。
            JobInvokeUtil.invokeMethod(scheduleJob);
        }
    }
}
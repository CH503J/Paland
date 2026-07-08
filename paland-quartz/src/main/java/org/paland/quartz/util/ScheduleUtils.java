package org.paland.quartz.util;

import org.paland.quartz.job.QuartzDisallowConcurrentExecution;
import org.paland.quartz.job.QuartzJobExecution;
import org.quartz.*;
import org.paland.quartz.domain.SysJob;

/**
 * Quartz 调度工具类。
 *
 * <p>负责封装 Quartz 底层 API，统一完成任务键生成、任务注册、
 * Trigger 创建、错失策略处理等操作，为业务层提供统一的调度能力。</p>
 *
 * <p>业务层无需直接操作 Quartz API，只需调用本工具类即可完成
 * 定时任务与 Quartz 调度器之间的交互。</p>
 *
 * @author ChenJun
 */
public class ScheduleUtils {

    /**
     * JobDataMap 中保存的任务 ID。
     */
    public static final String TASK_ID_KEY = "TASK_ID_KEY";
    /**
     * JobDataMap 中保存的任务分组。
     */
    public static final String TASK_GROUP_KEY = "TASK_GROUP_KEY";

    /**
     * 构建 Quartz JobKey。
     *
     * @param jobId 任务 ID
     * @param jobGroup 任务分组
     * @return JobKey
     */
    public static JobKey getJobKey(String jobId, String jobGroup) {
        return JobKey.jobKey("TASK_" + jobId, jobGroup);
    }

    /**
     * 构建 Quartz TriggerKey。
     *
     * @param jobId 任务 ID
     * @param jobGroup 任务分组
     * @return TriggerKey
     */
    public static TriggerKey getTriggerKey(String jobId, String jobGroup) {
        return TriggerKey.triggerKey("TASK_" + jobId, jobGroup);
    }

    /**
     * 创建或更新 Quartz 定时任务。
     *
     * <p>根据业务配置创建 JobDetail 和 Trigger，
     * 并注册到 Quartz 调度器中。如果任务已存在，则先删除旧任务，
     * 再重新创建，使最新配置立即生效。</p>
     *
     * @param scheduler Quartz 调度器
     * @param job 任务配置
     * @throws SchedulerException Quartz 操作异常
     */
    public static void createScheduleJob(Scheduler scheduler, SysJob job) throws SchedulerException {
        Class<? extends Job> jobClass = getQuartzJobClass(job);
        String jobId = job.getId();
        String jobGroup = job.getJobGroup();
        JobKey jobKey = getJobKey(jobId, jobGroup);

        // 1. 构建 JobDetail
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(jobKey)
                .build();

        // 2. 将扁平化的基础标量数据存入，保证集群反序列化永远安全
        jobDetail.getJobDataMap().put(TASK_ID_KEY, jobId);
        jobDetail.getJobDataMap().put(TASK_GROUP_KEY, jobGroup);

        // 3. 构建 Cron 表达式调度器
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
        cronScheduleBuilder = handleMisfirePolicy(job, cronScheduleBuilder);

        // 4. 构建 Trigger
        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(getTriggerKey(jobId, jobGroup))
                .withSchedule(cronScheduleBuilder)
                .build();

        // 5. 哲学修正：如果存在旧任务，先安全移除
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
        }

        // 6. 注册入内核
        scheduler.scheduleJob(jobDetail, trigger);

        // 7. 动态同步状态
        if (job.getStatus() == 1) {
            scheduler.pauseJob(jobKey);
        }
    }

    /**
     * 根据任务配置设置 Quartz 错失执行策略。
     */
    private static CronScheduleBuilder handleMisfirePolicy(SysJob job, CronScheduleBuilder cb) {
        switch (job.getMisfirePolicy()) {
            case 1: return cb.withMisfireHandlingInstructionIgnoreMisfires();
            case 2: return cb.withMisfireHandlingInstructionFireAndProceed();
            case 3: return cb.withMisfireHandlingInstructionDoNothing();
            default: return cb;
        }
    }

    /**
     * 根据并发策略选择对应的 Quartz Job 实现。
     */
    private static Class<? extends Job> getQuartzJobClass(SysJob sysJob) {
        return sysJob.getConcurrent() == 0 ? QuartzJobExecution.class : QuartzDisallowConcurrentExecution.class;
    }
}
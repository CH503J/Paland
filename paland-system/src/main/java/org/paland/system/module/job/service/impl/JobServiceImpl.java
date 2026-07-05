package org.paland.system.module.job.service.impl;

import lombok.RequiredArgsConstructor;
import org.paland.system.module.job.service.JobService;
import org.paland.system.module.job.task.QuartzJobExecution;
import org.quartz.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final Scheduler scheduler;

    /**
     * 添加一个定时任务
     *
     * @param jobName      任务名
     * @param jobGroup     任务组名
     * @param invokeTarget 执行方法
     * @param cron         时间表达式
     */
    @Override
    public void addJob(String jobName, String jobGroup, String invokeTarget, String cron) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup); // 显式声明 TriggerKey

        // 1. 构建 JobDetail
        JobDetail jobDetail = JobBuilder.newJob(QuartzJobExecution.class)
                .withIdentity(jobKey)
                .build();
        jobDetail.getJobDataMap().put("invokeTarget", invokeTarget);

        // 2. 构建 CronScheduleBuilder 并设置【关键】防卡死策略
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cron)
                .withMisfireHandlingInstructionDoNothing(); // 核心：错过不补跑，防止暂停恢复后卡死

        // 3. 构建 Trigger
        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey) // 统一使用 triggerKey 标识
                .withSchedule(scheduleBuilder)
                .build();

        // 4. 清理旧任务（防冲突）
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
        }

        // 5. 调度任务
        scheduler.scheduleJob(jobDetail, trigger);
    }

    /**
     * 暂停一个定时任务
     *
     * @param jobName  任务名
     * @param jobGroup 任务组名
     */
    @Override
    public void pauseJob(String jobName, String jobGroup) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);

        // 健壮性检查：存在才操作，防止因命名空间不一致引发报错
        if (scheduler.checkExists(jobKey)) {
            scheduler.pauseJob(jobKey);
        }
        if (scheduler.checkExists(triggerKey)) {
            scheduler.pauseTrigger(triggerKey);
        }
    }

    /**
     * 恢复一个定时任务
     *
     * @param jobName  任务名
     * @param jobGroup 任务组名
     */
    @Override
    public void resumeJob(String jobName, String jobGroup) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);

        // 1. 恢复 Job 状态
        if (scheduler.checkExists(jobKey)) {
            scheduler.resumeJob(jobKey);
        }

        // 2. 恢复 Trigger 状态
        if (scheduler.checkExists(triggerKey)) {
            scheduler.resumeTrigger(triggerKey);
        }
        // 【移除 rescheduleJob】因为事务解耦后，单纯的 resumeTrigger 就足以将状态从 ACQUIRED 刷新并正常推动执行！
    }

    /**
     * 更新定时任务时间表达式
     *
     * @param jobName  任务名
     * @param jobGroup 任务组名
     * @param newCron  新的时间表达式
     */
    @Override
    public void updateCron(String jobName, String jobGroup, String newCron) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);

        // 1. 可选：先检查是否存在
        if (!scheduler.checkExists(triggerKey)) {
            throw new SchedulerException("Trigger 不存在: " + triggerKey);
        }

        // 2. 先暂停（强烈建议）
        scheduler.pauseTrigger(triggerKey);

        // 3. 删除旧 Trigger（最稳妥方式）
        scheduler.unscheduleJob(triggerKey);

        // 4. 创建新的 Trigger 并关联 Job
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(newCron)
                .withMisfireHandlingInstructionDoNothing();   // 推荐：错过不补跑

        CronTrigger newTrigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .withSchedule(scheduleBuilder)
                .forJob(jobKey)          // ← 关键！必须关联 Job
                .build();

        // 5. 重新调度
        scheduler.scheduleJob(newTrigger);

        System.out.println("Cron 更新成功: " + jobName + " → " + newCron);
    }

    /**
     * 删除一个定时任务（同时删除关联的Trigger）
     *
     * @param jobName  任务名
     * @param jobGroup 任务组名
     */
    @Override
    public void deleteJob(String jobName, String jobGroup) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
        }
    }


}
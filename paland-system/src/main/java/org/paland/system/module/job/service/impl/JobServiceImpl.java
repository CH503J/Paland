package org.paland.system.module.job.service.impl;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.paland.system.module.job.service.JobService;
import org.paland.system.module.job.task.QuartzJobExecution;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
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

        JobDetail jobDetail = JobBuilder.newJob(QuartzJobExecution.class)
                .withIdentity(jobKey)
                .build();
        jobDetail.getJobDataMap().put("invokeTarget", invokeTarget);

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobName, jobGroup)
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .build();

        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
        }
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
        scheduler.pauseJob(JobKey.jobKey(jobName, jobGroup));
    }

    /**
     * 恢复一个定时任务
     *
     * @param jobName  任务名
     * @param jobGroup 任务组名
     */
    @Override
    public void resumeJob(String jobName, String jobGroup) throws SchedulerException {
        scheduler.resumeJob(JobKey.jobKey(jobName, jobGroup));
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
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
        CronTrigger newTrigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .withSchedule(CronScheduleBuilder.cronSchedule(newCron))
                .build();
        scheduler.rescheduleJob(triggerKey, newTrigger);
    }
}
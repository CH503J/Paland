package org.paland.system.module.job.service;

import org.quartz.SchedulerException;

public interface JobService {

    /**
     * 添加一个定时任务
     *
     * @param jobName  任务名
     * @param jobGroup 任务组名
     * @param cron     时间表达式
     */
    void addJob(String jobName, String jobGroup, String invokeTarget, String cron) throws SchedulerException;

    /**
     * 暂停一个定时任务
     *
     * @param jobName  任务名
     * @param jobGroup 任务组名
     */
    void pauseJob(String jobName, String jobGroup) throws SchedulerException;

    /**
     * 恢复一个定时任务
     *
     * @param jobName  任务名
     * @param jobGroup 任务组名
     */
    void resumeJob(String jobName, String jobGroup) throws SchedulerException;

    /**
     * 更新定时任务时间表达式
     *
     * @param jobName  任务名
     * @param jobGroup 任务组名
     * @param newCron  新的时间表达式
     */
    void updateCron(String jobName, String jobGroup, String newCron) throws SchedulerException;

    /**
     * 删除一个定时任务（同时删除关联的Trigger）
     *
     * @param jobName  任务名
     * @param jobGroup 任务组名
     */
    void deleteJob(String jobName, String jobGroup) throws SchedulerException;

}

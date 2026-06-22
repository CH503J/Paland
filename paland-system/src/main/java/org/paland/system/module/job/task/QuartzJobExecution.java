package org.paland.system.module.job.task;

import lombok.extern.slf4j.Slf4j;
import org.paland.system.module.job.util.JobInvokeUtil;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

@Slf4j
public class QuartzJobExecution extends QuartzJobBean {

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        String invokeTarget = (String) context.getMergedJobDataMap().get("invokeTarget");
        try {
            JobInvokeUtil.invokeMethod(invokeTarget);
        } catch (Exception e) {
            log.error("定时任务执行异常，invokeTarget={}", invokeTarget, e);
        }
    }
}
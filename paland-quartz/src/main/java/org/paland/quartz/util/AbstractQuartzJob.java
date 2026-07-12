package org.paland.quartz.util;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Quartz 任务抽象基类。
 *
 * <p>采用模板方法模式封装定时任务的公共执行流程，
 * 统一处理任务执行前准备、执行、异常处理及执行后收尾等逻辑，
 * 子类只需实现具体的业务执行方法即可。</p>
 *
 * <p>后续可在该类中统一扩展任务日志、执行统计、告警通知等公共能力，
 * 避免每个定时任务重复编写相同代码。</p>
 *
 * @author ChenJun
 */
@Slf4j
public abstract class AbstractQuartzJob implements Job {

    /**
     * 保存任务开始执行时间的上下文 Key。
     */
    private static final String KEY_START_TIME = "JOB_START_TIME";

    /**
     * Quartz 任务统一执行入口。
     *
     * <p>定义定时任务的标准执行流程：
     * 执行前处理 → 执行业务逻辑 → 执行后处理。</p>
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        before(context);
        try {
            doExecute(context);
            after(context, null);
        } catch (Exception e) {
            log.error("定时任务执行遇到未知异常:", e);
            after(context, e);
            // 将异常继续抛给 Quartz，由调度器统一处理后续策略（如 Misfire、监听器等）。
            throw new JobExecutionException(e);
        }
    }

    /**
     * 执行前处理。
     *
     * <p>记录任务开始时间，并输出开始执行日志。</p>
     */
    protected void before(JobExecutionContext context) {
        context.put(KEY_START_TIME, System.currentTimeMillis());
        log.info("--- 定时任务 [{}] 开始执行 ---", context.getJobDetail().getKey().getName());
    }

    /**
     * 执行后处理。
     *
     * <p>统计任务执行耗时，根据执行结果输出运行日志。
     * 后续可在此统一记录任务执行日志、发送告警等。</p>
     */
    protected void after(JobExecutionContext context, Exception e) {
        Long startTime = (Long) context.get(KEY_START_TIME);
        long runMs = (startTime != null) ? (System.currentTimeMillis() - startTime) : 0L;
        String jobKeyName = context.getJobDetail().getKey().getName();

        if (e != null) {
            log.error("--- 定时任务 [{}] 执行失败！耗时: {} 毫秒，错误信息: {} ---", jobKeyName, runMs, e.getMessage());
            /* [生产环境待扩展]
               TODO：任务日志模块完成后，在此记录失败日志。
               建议采用异步方式持久化至 sys_job_log，避免阻塞 Quartz 工作线程。
            */
        } else {
            log.info("--- 定时任务 [{}] 执行成功！耗时: {} 毫秒 ---", jobKeyName, runMs);
            /* [生产环境待扩展]
               TODO：任务日志模块完成后，在此记录任务执行成功日志。
            */
        }
    }

    /**
     * 执行具体的业务逻辑。
     *
     * @param context Quartz 执行上下文
     * @throws Exception 业务执行过程中发生的异常
     */
    protected abstract void doExecute(JobExecutionContext context) throws Exception;
}
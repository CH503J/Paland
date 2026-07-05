package org.paland.admin.module.job;

import lombok.RequiredArgsConstructor;
import org.paland.common.result.Result;
import org.paland.system.module.job.service.JobService;
import org.quartz.SchedulerException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 验证定时任务机制是否正常跳动的示例控制器
 */
@RestController
@RequestMapping("/demo/job")
@RequiredArgsConstructor
public class DemoJobController {

    private final JobService jobService;

    /**
     * 添加一个定时任务
     *
     * @param cron 时间表达式
     */
    @PostMapping("/add")
    public Result<Void> addDemoJob(@RequestParam String cron) throws SchedulerException {
        jobService.addJob("demoLogJob", "DEFAULT", "demoLogTask.run", cron);
        return Result.success();
    }

    /**
     * 暂停一个定时任务
     */
    @PostMapping("/pause")
    public Result<Void> pauseDemoJob() throws SchedulerException {
        jobService.pauseJob("demoLogJob", "DEFAULT");
        return Result.success();
    }

    /**
     * 恢复一个定时任务
     */
    @PostMapping("/resume")
    public Result<Void> resumeDemoJob() throws SchedulerException {
        jobService.resumeJob("demoLogJob", "DEFAULT");
        return Result.success();
    }

    /**
     * 更新定时任务时间表达式
     *
     * @param cron 时间表达式
     */
    @PostMapping("/update")
    public Result<Void> updateDemoJob(@RequestParam String cron) throws SchedulerException {
        jobService.updateCron("demoLogJob", "DEFAULT", cron);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<Void> deleteDemoJob() throws SchedulerException {
        jobService.deleteJob("demoLogJob", "DEFAULT");
        return Result.success();
    }
}
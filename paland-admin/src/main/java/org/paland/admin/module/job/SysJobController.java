package org.paland.admin.module.job;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.paland.common.result.Result;
import org.paland.system.module.job.dto.SysJobCreateRequestDTO;
import org.paland.system.module.job.dto.SysJobUpdateRequestDTO;
import org.paland.system.module.job.service.SysJobService;
import org.paland.system.module.job.vo.SysJobResponseVO;
import org.quartz.SchedulerException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys/job")
@RequiredArgsConstructor
public class SysJobController {

    private final SysJobService sysJobService;

    /**
     * 创建任务
     *
     * @param dto
     * @return
     * @throws SchedulerException
     * PS: 任务创建时，cron 表达式不能为每秒执行一次，否则可能造成并发线程问题导致quartz不受控
     */
    @PostMapping("/create")
    public Result<SysJobResponseVO> createJob(@RequestBody SysJobCreateRequestDTO dto) throws SchedulerException {
        return Result.success(sysJobService.createJob(dto));
    }

    /**
     * 暂停任务
     *
     * @param id
     * @return
     * @throws SchedulerException
     */
    @PostMapping("/pause")
    public Result<SysJobResponseVO> pauseJob(@RequestParam Long id) throws SchedulerException {
        return Result.success(sysJobService.updateStatus(id, 0));
    }

    /**
     * 恢复任务
     *
     * @param id
     * @return
     * @throws SchedulerException
     */
    @PostMapping("/resume")
    public Result<SysJobResponseVO> resumeJob(@RequestParam Long id) throws SchedulerException {
        return Result.success(sysJobService.updateStatus(id, 1));
    }

    /**
     * 修改cron表达式
     *
     * @param id             任务id
     * @param cronExpression cron表达式
     * @return
     * @throws SchedulerException
     */
    @PostMapping("/updateCron")
    public Result<SysJobResponseVO> updateCron(@RequestParam Long id, @RequestParam String jobGroup, @RequestParam String cronExpression) throws SchedulerException {
        return Result.success(sysJobService.updateCron(id, jobGroup, cronExpression));
    }

    /**
     * 更新任务信息
     *
     * @param dto
     * @return
     * @throws SchedulerException
     */
    @PostMapping("/update")
    public Result<SysJobResponseVO> updateJob(@Valid @RequestBody SysJobUpdateRequestDTO dto) throws SchedulerException {
        return Result.success(sysJobService.updateJob(dto));
    }

    /**
     * 删除任务
     *
     * @param id
     * @return
     * @throws SchedulerException
     */
    @PostMapping("/delete")
    public Result<SysJobResponseVO> deleteJob(@RequestParam Long id) throws SchedulerException {
        return Result.success(sysJobService.deleteJob(id));
    }

    /**
     * 查询任务列表
     *
     * @return
     */
    @PostMapping("/list")
    public Result<List<SysJobResponseVO>> listJobs() {
        return Result.success(sysJobService.listJobs());
    }
}

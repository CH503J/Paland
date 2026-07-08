package org.paland.quartz.controller;

import lombok.RequiredArgsConstructor;
import org.paland.common.result.Result;
import org.paland.quartz.domain.dto.SysJobAddDTO;
import org.paland.quartz.domain.dto.SysJobUpdateDTO;
import org.paland.quartz.service.ISysJobService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 定时任务管理控制器。
 *
 * <p>作为定时任务模块对外提供的 REST 接口入口，
 * 负责接收前端发送的请求、完成参数校验，并将业务处理交给业务层（Service）完成。</p>
 *
 * <p>Controller 本身不处理具体业务逻辑，也不直接操作 Quartz，
 * 只负责请求与响应的转换，保证各层职责清晰、职责单一。</p>
 *
 * @author ChenJun
 */
@RestController
@RequestMapping("/sys/job")
@RequiredArgsConstructor
public class SysJobController {

    private final ISysJobService jobService;

    /**
     * 创建定时任务。
     *
     * <p>接收前端提交的任务信息，校验请求参数，
     * 并交由业务层完成任务创建及 Quartz 注册。</p>
     */
    @PostMapping("/create")
    public Result<Void> create(@Validated @RequestBody SysJobAddDTO addDTO) {
        jobService.createJob(addDTO);
        return Result.success();
    }

    /**
     * 更新定时任务。
     *
     * <p>接收前端提交的任务信息，校验请求参数，
     * 并交由业务层完成任务更新及 Quartz 注册。</p>
     */
    @PostMapping("/update")
    public Result<Void> update(@Validated @RequestBody SysJobUpdateDTO addDTO) {
        jobService.updateJob(addDTO);
        return Result.success();
    }

    /**
     * 删除定时任务。
     *
     * <p>接收前端提交的任务信息，校验请求参数，
     * 并交由业务层完成任务删除及 Quartz 注销。</p>
     */
    @PostMapping("/delete")
    public Result<Void> delete(@Validated @RequestParam("id") String id) {
        jobService.deleteJob(id);
        return Result.success();
    }

    /**
     * 暂停定时任务。
     *
     * <p>接收前端提交的任务信息，校验请求参数，
     * 并交由业务层完成任务暂停。</p>
     */
    @PostMapping("/pause")
    public Result<Void> pause(@Validated @RequestParam("id") String id) {
        jobService.pauseJob(id);
        return Result.success();
    }

    /**
     * 恢复定时任务。
     *
     * <p>接收前端提交的任务信息，校验请求参数，
     * 并交由业务层完成任务恢复。</p>
     */
    @PostMapping("/resume")
    public Result<Void> resume(@Validated @RequestParam("id") String id) {
        jobService.resumeJob(id);
        return Result.success();
    }

    /**
     * 立即执行一次定时任务。
     *
     * <p>接收前端提交的任务信息，校验请求参数，
     * 并交由业务层完成任务立即执行一次。</p>
     */
    @PostMapping("/runOnce")
    public Result<Void> runOnce(@Validated @RequestParam("id") String id) throws Exception {
        jobService.runOnce(id);
        return Result.success();
    }
}
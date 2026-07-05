package org.paland.system.module.job.service;

import org.paland.system.module.job.dto.SysJobCreateRequestDTO;
import org.paland.system.module.job.dto.SysJobUpdateRequestDTO;
import org.paland.system.module.job.vo.SysJobResponseVO;
import org.quartz.SchedulerException;

import java.util.List;

public interface SysJobService {

    /**
     * 创建定时任务（先存PAUSE状态写库，再注册到Quartz）
     */
    SysJobResponseVO createJob(SysJobCreateRequestDTO dto) throws SchedulerException;

    /**
     * 更新任务状态
     *
     * @param id
     * @throws SchedulerException
     */
    SysJobResponseVO updateStatus(Long id, Integer status) throws SchedulerException;

    /**
     * 修改cron表达式
     */
    SysJobResponseVO updateCron(Long id, String jobGroup, String cronExpression) throws SchedulerException;

    /**
     * 更新任务信息
     */
    SysJobResponseVO updateJob(SysJobUpdateRequestDTO dto);

    /**
     * 删除任务（同时从Quartz中移除）
     */
    SysJobResponseVO deleteJob(Long id) throws SchedulerException;

    /**
     * 查询任务列表
     */
    List<SysJobResponseVO> listJobs();


}
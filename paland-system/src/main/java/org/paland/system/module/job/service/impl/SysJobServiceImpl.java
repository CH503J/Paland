package org.paland.system.module.job.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.paland.common.exception.BusinessException;
import org.paland.common.result.ResultCode;
import org.paland.system.module.job.dto.SysJobCreateRequestDTO;
import org.paland.system.module.job.dto.SysJobUpdateRequestDTO;
import org.paland.system.module.job.entity.SysJob;
import org.paland.system.module.job.mapper.SysJobMapper;
import org.paland.system.module.job.service.JobService;
import org.paland.system.module.job.service.SysJobService;
import org.paland.system.module.job.util.CronUtil;
import org.paland.system.module.job.vo.SysJobResponseVO;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class SysJobServiceImpl implements SysJobService {

    private final SysJobMapper sysJobMapper;
    private final JobService jobService;

    /**
     * 创建定时任务
     *
     * @param dto 创建任务的请求DTO
     * @return 创建后的任务信息
     * @throws SchedulerException 当Quartz任务操作失败时抛出
     */
    @Override
    // 移除方法上的 @Transactional 注解！
    public SysJobResponseVO createJob(SysJobCreateRequestDTO dto) throws SchedulerException {
        if (CronUtil.isInvalid(dto.getCronExpression())) {
            throw new BusinessException(ResultCode.INVALID_CRON_EXPRESSION, "cron表达式[" + dto.getCronExpression() + "]无效");
        }

        // 1. 先落库，提交 Spring 事务
        SysJob sysJob = saveSysJobInTx(dto);

        // 2. 事务完成后，操作 Quartz
        String quartzName = "job_" + sysJob.getId();
        jobService.addJob(quartzName,
                sysJob.getJobGroup(),
                sysJob.getInvokeTarget(),
                sysJob.getCronExpression());

        // 默认为暂停状态
        if (sysJob.getStatus() == 0) {
            jobService.pauseJob(quartzName, sysJob.getJobGroup());
        }

        SysJobResponseVO response = new SysJobResponseVO();
        BeanUtil.copyProperties(sysJob, response);
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public SysJob saveSysJobInTx(SysJobCreateRequestDTO dto) {
        SysJob sysJob = new SysJob();
        BeanUtil.copyProperties(dto, sysJob);
        sysJob.setStatus(0); // 先统一存为暂停状态
        if (StrUtil.isBlank(dto.getJobGroup())) {
            sysJob.setJobGroup("DEFAULT");
        }
        sysJobMapper.insert(sysJob);
        return sysJob;
    }

    /**
     * 更新定时任务状态
     *
     * @param id     任务id
     * @param status 任务状态（0：暂停，1：恢复）
     * @return 更新后的任务信息
     * @throws SchedulerException 当Quartz任务操作失败时抛出
     */
    @Override
    // 移除方法上的 @Transactional 注解！
    public SysJobResponseVO updateStatus(Long id, Integer status) throws SchedulerException {
        if (status != 0 && status != 1) {
            throw new BusinessException(ResultCode.JOB_STATUS_INVALID, "定时任务状态[" + status + "]无效");
        }

        // 1. 在一个独立的短事务中更新数据库，并立刻提交释放锁
        SysJob sysJob = updateJobStatusInTx(id, status);

        // 2. 事务提交后，再安全地去操作 Quartz（此时没有 Spring 事务纠缠，状态绝对不卡死）
        String quartzName = "job_" + sysJob.getId();
        if (status == 0) {
            jobService.pauseJob(quartzName, sysJob.getJobGroup());
        } else {
            jobService.resumeJob(quartzName, sysJob.getJobGroup());
        }

        SysJobResponseVO vo = new SysJobResponseVO();
        BeanUtil.copyProperties(sysJob, vo);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public SysJob updateJobStatusInTx(Long id, Integer status) {
        SysJob sysJob = sysJobMapper.selectById(id);
        if (sysJob == null) {
            throw new BusinessException(ResultCode.JOB_NOT_FOUND, "定时任务[" + id + "]不存在");
        }
        sysJob.setStatus(status);
        sysJobMapper.updateById(sysJob);
        return sysJob;
    }

    /**
     * 修改cron表达式
     *
     * @param id             任务id
     * @param cronExpression 新的cron表达式
     * @return 更新后的任务信息
     * @throws SchedulerException 当Quartz任务操作失败时抛出
     */
    @Override
    @Transactional
    public SysJobResponseVO updateCron(Long id, String jobGroup, String cronExpression) throws SchedulerException {
        SysJob sysJob = sysJobMapper.selectById(id);
        if (sysJob == null) {
            throw new BusinessException(ResultCode.JOB_NOT_FOUND, "定时任务[" + id + "]不存在");
        }

        if (CronUtil.isInvalid(cronExpression)) {
            throw new BusinessException(ResultCode.INVALID_CRON_EXPRESSION, "cron表达式无效");
        }

        if (StrUtil.isBlank(jobGroup)) {
            jobGroup = sysJob.getJobGroup();
        }

        String quartzName = "job_" + sysJob.getId();

        // 先暂停
        jobService.pauseJob(quartzName, jobGroup);

        // 更新 Quartz
        jobService.updateCron(quartzName, jobGroup, cronExpression);

        // 更新业务表
        sysJob.setJobGroup(jobGroup);
        sysJob.setCronExpression(cronExpression);
        sysJobMapper.updateById(sysJob);

        // 如果原来是启用状态，则恢复
        if (Objects.equals(sysJob.getStatus(), 1)) {
            jobService.resumeJob(quartzName, jobGroup);
        }

        SysJobResponseVO vo = new SysJobResponseVO();
        BeanUtil.copyProperties(sysJob, vo);
        return vo;
    }

    /**
     * 更新定时任务
     *
     * @param dto 更新任务的请求DTO
     * @return 更新后的任务信息
     */
    @Override
    public SysJobResponseVO updateJob(SysJobUpdateRequestDTO dto) {

        SysJob sysJob = sysJobMapper.selectById(dto.getId());
        if (sysJob == null) {
            throw new BusinessException(ResultCode.JOB_NOT_FOUND, "定时任务[" + dto.getId() + "]不存在");
        }
        if (StrUtil.isNotBlank(dto.getJobName())) {
            sysJob.setJobName(dto.getJobName());
        }
        if (StrUtil.isNotBlank(dto.getRemark())) {
            sysJob.setRemark(dto.getRemark());
        }
        sysJobMapper.updateById(sysJob);

        SysJobResponseVO vo = new SysJobResponseVO();
        BeanUtil.copyProperties(sysJob, vo);
        return vo;
    }

    /**
     * 删除定时任务
     *
     * @param id 任务id
     * @return 删除后的任务信息
     * @throws SchedulerException 当Quartz任务操作失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysJobResponseVO deleteJob(Long id) throws SchedulerException {
        SysJob sysJob = sysJobMapper.selectById(id);
        if (sysJob == null) {
            throw new BusinessException(ResultCode.JOB_NOT_FOUND, "定时任务[" + id + "]不存在");
        }
        String jobName = "job_" + sysJob.getId();
        jobService.deleteJob(jobName, sysJob.getJobGroup());
        sysJobMapper.deleteById(id);
        log.info("删除定时任务: jobName={}, jobGroup={}", jobName, sysJob.getJobGroup());
        SysJobResponseVO response = new SysJobResponseVO();
        BeanUtil.copyProperties(sysJob, response);
        return response;
    }

    @Override
    public List<SysJobResponseVO> listJobs() {
        return List.of();
    }
}

package org.paland.quartz.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.paland.common.exception.BusinessException;
import org.paland.common.result.ResultCode;
import org.paland.quartz.domain.SysJob;
import org.paland.quartz.domain.dto.SysJobAddDTO;
import org.paland.quartz.domain.dto.SysJobUpdateDTO;
import org.paland.quartz.mapper.SysJobMapper;
import org.paland.quartz.service.ISysJobService;
import org.paland.quartz.util.CronUtils;
import org.paland.quartz.util.ScheduleUtils;
import org.quartz.JobDataMap;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 定时任务业务实现类。
 *
 * <p>负责定时任务的业务管理，包括任务的新增、修改、删除、启停及手动执行等操作。
 * 在处理业务数据的同时，同步维护 Quartz 调度器中的任务，保证业务配置与调度内核保持一致。</p>
 *
 * <p>本类属于业务层，不负责具体的 Quartz API 封装，
 * 与 Quartz 的交互统一由 {@code ScheduleUtils} 等工具类完成。</p>
 *
 * @author ChenJun
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysJobServiceImpl extends ServiceImpl<SysJobMapper, SysJob> implements ISysJobService {

    private final Scheduler scheduler;

    /**
     * 初始化 Quartz 调度器。
     *
     * <p>项目启动后，将数据库中的定时任务重新加载到 Quartz 调度器，
     * 保证调度内核与业务配置保持一致。</p>
     *
     * <p>由于 Quartz 的运行状态属于内存数据，应用重启或集群节点启动后
     * 都需要重新完成任务注册。</p>
     */
    @PostConstruct
    public void init() {
        try {
            List<SysJob> jobList = this.list();
            for (SysJob job : jobList) {
                ScheduleUtils.createScheduleJob(scheduler, job);
            }
            log.info(">>>> [PaLand Quartz] 定时任务内核同步成功，同步加载 {} 个任务 >>>>", jobList.size());
        } catch (Exception e) {
            log.error("定时任务内核同步失败！", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createJob(SysJobAddDTO addDTO) {

        // 校验 Cron 表达式
        if (!CronUtils.isValid(addDTO.getCronExpression())) {
            throw new BusinessException(ResultCode.INVALID_CRON_EXPRESSION, "Cron表达式非法");
        }

        // DTO 转 Entity
        SysJob scheduleJob = new SysJob();
        BeanUtils.copyProperties(addDTO, scheduleJob);
        scheduleJob.setStatus(1);  // 默认暂停

        // 保存定时任务到业务表
        boolean success = this.save(scheduleJob);
        if (!success) {
            throw new BusinessException(ResultCode.JOB_OPERATION_FAILED, "任务创建失败");
        }

        // 向 Quartz 中注册定时任务
        try {
            ScheduleUtils.createScheduleJob(scheduler, scheduleJob);
        } catch (SchedulerException e) {
            log.error("Quartz任务注册失败，任务ID：{}", scheduleJob.getId(), e);
            throw new BusinessException(ResultCode.JOB_OPERATION_FAILED, "定时任务内核注册失败");
        }

    }

    /**
     * 更新定时任务。
     *
     * <p>更新业务配置后，重新创建 Quartz 调度任务，
     * 使最新配置立即生效。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateJob(SysJobUpdateDTO jobDto) {
        // 1. 校验 Cron 表达式
        if (StringUtils.isNotBlank(jobDto.getCronExpression()) && !CronUtils.isValid(jobDto.getCronExpression())) {
            throw new BusinessException(ResultCode.INVALID_CRON_EXPRESSION, "Cron表达式非法");
        }

        // 2. 【关键步骤】必须先从数据库查出原有的数据库实体（包含原有的 jobGroup）
        SysJob job = this.getById(jobDto.getId());
        if (job == null) {
            throw new BusinessException(ResultCode.JOB_NOT_FOUND, "定时任务不存在");
        }

        // 3. 将 DTO 中允许修改的字段（如 cronExpression, invokeTarget 等）覆盖到 job 对象中
        // 此时 job 对象里的 jobGroup 依然是数据库里原本的值，安全无污染
        BeanUtils.copyProperties(jobDto, job);

        // 4. 更新数据库
        boolean success = this.updateById(job);
        if (!success) {
            throw new BusinessException(ResultCode.JOB_OPERATION_FAILED, "任务更新失败");
        }

        // 5. 更新 Quartz 内核
        try {
            // 这里使用的 job.getJobGroup() 是第 2 步从数据库里查出来的，绝对准确！
            scheduler.deleteJob(ScheduleUtils.getJobKey(job.getId(), job.getJobGroup()));
            ScheduleUtils.createScheduleJob(scheduler, job);
        } catch (SchedulerException e) {
            log.error("Quartz任务更新失败，任务ID：{}", job.getId(), e);
            throw new BusinessException(ResultCode.JOB_OPERATION_FAILED, "定时任务内核更新失败");
        }
    }

    /**
     * 删除定时任务。
     *
     * <p>删除业务数据的同时，从 Quartz 调度器中移除对应任务。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteJob(String id) {

        // 1. 获取任务
        SysJob sysJob = this.getById(id);

        // 2. 验证任务是否存在
        if (sysJob == null) {
            throw new BusinessException(ResultCode.JOB_NOT_FOUND, "定时任务不存在");
        }

        // 3. 删除任务
        boolean success = this.removeById(id);
        if (!success) {
            throw new BusinessException(ResultCode.JOB_OPERATION_FAILED, "任务删除失败");
        }

        // 4. 删除 Quartz 内核中的任务
        try {
            scheduler.deleteJob(ScheduleUtils.getJobKey(sysJob.getId(), sysJob.getJobGroup()));
        } catch (SchedulerException e) {
            log.error("Quartz任务删除失败，任务ID：{}", sysJob.getId(), e);
            throw new BusinessException(ResultCode.JOB_OPERATION_FAILED, "定时任务内核删除失败");
        }
    }

    /**
     * 暂停定时任务。
     *
     * <p>暂停任务时，将任务状态更新为暂停，并暂停 Quartz 内核中的任务。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pauseJob(String id) {

        SysJob sysJob = this.getById(id);
        sysJob.setStatus(1); // 假设 1 为暂停/禁用状态，与你 createJob 中一致
        boolean success = this.updateById(sysJob);
        if (!success) {
            throw new BusinessException(ResultCode.JOB_OPERATION_FAILED, "任务暂停失败");
        }

        try {
            scheduler.pauseJob(ScheduleUtils.getJobKey(sysJob.getId(), sysJob.getJobGroup()));
        } catch (SchedulerException e) {
            log.error("Quartz任务暂停失败，任务ID：{}", sysJob.getId(), e);
            throw new BusinessException(ResultCode.JOB_OPERATION_FAILED, "定时任务内核暂停失败");
        }
    }

    /**
     * 恢复定时任务。
     *
     * <p>恢复任务时，将任务状态更新为正常，并恢复 Quartz 内核中的任务。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resumeJob(String id) {

        SysJob sysJob = this.getById(id);
        sysJob.setStatus(0); // 假设 0 为启用/正常状态
        boolean success = this.updateById(sysJob);
        if (!success) {
            throw new BusinessException(ResultCode.JOB_OPERATION_FAILED, "任务恢复失败");
        }

        try {
            scheduler.resumeJob(ScheduleUtils.getJobKey(sysJob.getId(), sysJob.getJobGroup()));
        } catch (SchedulerException e) {
            log.error("Quartz任务恢复失败，任务ID：{}", sysJob.getId(), e);
            throw new BusinessException(ResultCode.JOB_OPERATION_FAILED, "定时任务内核恢复失败");
        }
    }

    /**
     * 立即执行一次定时任务。
     *
     * <p>执行一次任务时，将任务状态更新为正常，并恢复 Quartz 内核中的任务。</p>
     */
    @Override
    public void runOnce(String id) {
        SysJob sysJob = this.getById(id);
        try {
            JobDataMap dataMap = new JobDataMap();
            dataMap.put(ScheduleUtils.TASK_ID_KEY, sysJob.getId());
            dataMap.put(ScheduleUtils.TASK_GROUP_KEY, sysJob.getJobGroup());

            scheduler.triggerJob(ScheduleUtils.getJobKey(sysJob.getId(), sysJob.getJobGroup()), dataMap);
        } catch (SchedulerException e) {
            log.error("Quartz任务单次执行失败，任务ID：{}", sysJob.getId(), e);
            throw new BusinessException(ResultCode.JOB_EXECUTION_FAILED, "定时任务内核触发失败");
        }
    }

    /**
     * 校验 Cron 表达式是否有效。
     */
    @Override
    public boolean checkCronExpressionIsValid(String cronExpression) {
        return CronUtils.isValid(cronExpression);
    }
}
package org.paland.quartz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.paland.quartz.domain.SysJob;
import org.paland.quartz.domain.dto.SysJobAddDTO;
import org.paland.quartz.domain.dto.SysJobUpdateDTO;
import org.quartz.SchedulerException;

public interface ISysJobService extends IService<SysJob> {

    /**
     * 新增任务
     */
    void createJob(SysJobAddDTO addDTO);

    /**
     * 更新任务
     */
    void updateJob(SysJobUpdateDTO job);

    /**
     * 删除任务
     */
    void deleteJob(String id);

    /**
     * 暂停任务
     */
    void pauseJob(String id);

    /**
     * 恢复任务
     */
    void resumeJob(String id);

    /**
     * 立即运行一次任务
     */
    void runOnce(String id) throws SchedulerException;

    /**
     * 校验Cron表达式有效性
     */
    boolean checkCronExpressionIsValid(String cronExpression);

}

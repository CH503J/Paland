package org.paland.quartz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.paland.quartz.domain.SysJob;

/**
 * 定时任务数据访问层。
 *
 * <p>负责定时任务业务数据（sys_job 表）的数据库操作。
 * 继承 MyBatis-Plus 提供的 {@code BaseMapper}，
 * 默认支持常用的增删改查操作，如有复杂查询可在此扩展自定义 SQL。</p>
 *
 * @author ChenJun
 */
@Mapper
public interface SysJobMapper extends BaseMapper<SysJob> {
}
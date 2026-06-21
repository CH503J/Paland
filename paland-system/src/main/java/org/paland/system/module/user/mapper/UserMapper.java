package org.paland.system.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.paland.system.module.user.entity.User;

/**
 * 用户表 Mapper 接口
 * <p>
 * 继承 BaseMapper 后自动拥有基础的增删改查方法（selectById、insert、updateById等），
 * 不需要手写任何 SQL；如果以后有复杂查询，再在这里加自定义方法。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
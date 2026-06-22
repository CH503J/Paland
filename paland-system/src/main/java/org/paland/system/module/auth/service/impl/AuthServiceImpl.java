package org.paland.system.module.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.paland.common.exception.BusinessException;
import org.paland.common.result.ResultCode;
import org.paland.system.module.auth.dto.LoginRequestDTO;
import org.paland.system.module.auth.dto.RegisterRequestDTO;
import org.paland.system.module.auth.service.AuthService;
import org.paland.system.module.auth.vo.LoginResponseVO;
import org.paland.system.module.user.entity.User;
import org.paland.system.module.user.mapper.UserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public void register(RegisterRequestDTO request) {
        // 1. 校验两次密码是否一致
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "两次输入的密码不一致");
        }

        // 2. 校验用户名是否已存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }

        // 3. 构造用户对象，密码用BCrypt加密后存储
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));
        user.setNickname(
                request.getNickname() != null && !request.getNickname().isBlank()
                        ? request.getNickname()
                        : request.getUsername()
        );
        user.setStatus(1);

        // 4. 插入数据库
        userMapper.insert(user);
    }

    @Override
    public LoginResponseVO login(LoginRequestDTO request) {
        // 1. 根据用户名查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 2. 校验账号状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用");
        }

        // 3. 校验密码
        if (!bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户名或密码错误");
        }

        // 4. 登录成功，生成token（Sa-Token会自动存入Redis）
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();

        // 5. 构造返回数据
        return LoginResponseVO.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .build();
    }
}
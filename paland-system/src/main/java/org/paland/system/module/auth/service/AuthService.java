package org.paland.system.module.auth.service;

import org.paland.system.module.auth.dto.LoginRequestDTO;
import org.paland.system.module.auth.dto.RegisterRequestDTO;
import org.paland.system.module.auth.vo.LoginResponseVO;

/**
 * 登录注册服务
 */
public interface AuthService {

    /**
     * 注册新用户
     *
     * @param request 注册请求参数
     */
    void register(RegisterRequestDTO request);

    /**
     * 登录
     *
     * @param request 登录请求参数
     * @return 登录成功后的token及用户信息
     */
    LoginResponseVO login(LoginRequestDTO request);
}
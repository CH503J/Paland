package org.paland.system.module.auth.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.paland.common.result.Result;
import org.paland.system.module.auth.dto.LoginRequestDTO;
import org.paland.system.module.auth.dto.RegisterRequestDTO;
import org.paland.system.module.auth.service.AuthService;
import org.paland.system.module.auth.vo.LoginResponseVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequestDTO request) {
        authService.register(request);
        return Result.success();
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<LoginResponseVO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseVO response = authService.login(request);
        return Result.success(response);
    }
}
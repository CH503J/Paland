package org.paland.system.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    // 注册 Sa-Token 拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册路由拦截器，自定义认证规则
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 默认拦截所有路由进行登录校验
            SaRouter.match("/**")
                    // 排除登录和注册接口（白名单）
                    .notMatch("/auth/login", "/auth/register")
                    // 排除你提到的测试任务接口或其它放行接口
                    // .notMatch("/job/**")
                    // 执行校验逻辑：未登录则抛出异常
                    .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }
}

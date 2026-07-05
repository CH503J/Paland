package org.paland.system.module.job.util;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;

import java.lang.reflect.Method;

import lombok.experimental.UtilityClass;

@UtilityClass // Java高级技巧：使工具类变为final，自动生成私有构造器
public class JobInvokeUtil {

    /**
     * 支持 beanName.methodName('param1', 123) 格式，或纯无参调用
     */
    public static void invokeMethod(String invokeTarget) throws Exception {
        if (invokeTarget == null || invokeTarget.isBlank()) {
            throw new IllegalArgumentException("invokeTarget不能为空");
        }

        // 规范化：如果老项目带参数，或者未来需要带参数
        int dotIndex = invokeTarget.indexOf(".");
        if (dotIndex == -1) {
            throw new IllegalArgumentException("格式错误，应为 beanName.methodName");
        }

        String beanName = invokeTarget.substring(0, dotIndex);
        String methodName = invokeTarget.substring(dotIndex + 1);

        // 处理无参的括号情况，如 demoLogTask.run()
        if (methodName.endsWith("()")) {
            methodName = methodName.substring(0, methodName.length() - 2);
        }

        Object bean = SpringUtil.getBean(beanName);

        // 核心安全提升：使用原生或Hutool的ReflectUtil，能自动穿透Spring的CGLIB代理类找到真实方法
        Method method = ReflectUtil.getMethodOfObj(bean, methodName);
        if (method == null) {
            throw new NoSuchMethodException("在Bean [" + beanName + "] 中未找到方法 [" + methodName + "]");
        }

        // 执行调用
        ReflectUtil.invoke(bean, method);
    }
}
package org.paland.system.module.job.util;

import cn.hutool.extra.spring.SpringUtil;

import java.lang.reflect.Method;

public class JobInvokeUtil {

    /**
     * 根据 "beanName.methodName" 格式的字符串，反射调用对应Bean的无参方法
     */
    public static void invokeMethod(String invokeTarget) throws Exception {
        if (invokeTarget == null || invokeTarget.isBlank()) {
            throw new IllegalArgumentException("invokeTarget不能为空");
        }
        int dotIndex = invokeTarget.indexOf(".");
        if (dotIndex == -1) {
            throw new IllegalArgumentException("invokeTarget格式错误，应为 beanName.methodName，实际为：" + invokeTarget);
        }
        String beanName = invokeTarget.substring(0, dotIndex);
        String methodName = invokeTarget.substring(dotIndex + 1);

        Object bean = SpringUtil.getBean(beanName);
        Method method = bean.getClass().getMethod(methodName);
        method.invoke(bean);
    }
}
package org.paland.quartz.util;

import lombok.extern.slf4j.Slf4j;
import org.paland.quartz.domain.SysJob;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * 定时任务调用工具类。
 *
 * <p>负责解析任务配置中的调用目标（invokeTarget），
 * 并通过 Spring Bean 或反射方式执行对应的方法。</p>
 *
 * <p>支持以下调用方式：</p>
 * <ul>
 *     <li>Spring Bean：palandTask.run()</li>
 *     <li>Spring Bean（带参数）：palandTask.run('test', 1)</li>
 *     <li>全限定类名：org.paland.task.TestTask.run()</li>
 * </ul>
 *
 * <p>为保证系统安全，仅允许反射调用指定包路径下的类，
 * 防止执行任意类或恶意代码。</p>
 *
 * @author ChenJun
 */
@Slf4j
public class JobInvokeUtil {

    /**
     * 允许反射调用的包名前缀。
     */
    private static final String ALLOW_CLASS_PREFIX = "org.paland.";

    /**
     * 根据任务配置执行目标方法。
     *
     * <p>支持 Spring Bean 和全限定类名两种调用方式，
     * 并在执行前进行必要的安全校验。</p>
     */
    public static void invokeMethod(SysJob scheduleJob) throws Exception {
        String invokeTarget = scheduleJob.getInvokeTarget();
        String beanName = getBeanName(invokeTarget);
        String methodName = getMethodName(invokeTarget);
        List<Object[]> methodParams = getMethodParams(invokeTarget);

        Object bean;
        if (isValidClassName(beanName)) {
            // 生产环境安全合规检查
            if (!beanName.startsWith(ALLOW_CLASS_PREFIX)) {
                throw new SecurityException("生产环境安全拦截：不允许反射调用非系统目标类 -> " + beanName);
            }
            bean = Class.forName(beanName).getDeclaredConstructor().newInstance();
        } else {
            bean = SpringUtils.getBean(beanName);
        }
        invokeMethod(bean, methodName, methodParams);
    }

    /**
     * 通过反射调用目标方法。
     */
    private static void invokeMethod(Object bean, String methodName, List<Object[]> methodParams) throws Exception {
        if (methodParams != null && !methodParams.isEmpty()) {
            Class<?>[] paramTypes = getMethodParamsType(methodParams);
            Object[] paramValues = getMethodParamsValue(methodParams);
            Method method = bean.getClass().getDeclaredMethod(methodName, paramTypes);
            ReflectionUtils.makeAccessible(method);
            method.invoke(bean, paramValues);
        } else {
            Method method = bean.getClass().getDeclaredMethod(methodName);
            ReflectionUtils.makeAccessible(method);
            method.invoke(bean);
        }
    }

    /**
     * 判断调用目标是否为全限定类名。
     *
     * @param invokeTarget 调用目标
     * @return true：全限定类名；false：Spring Bean 名称
     */
    public static boolean isValidClassName(String invokeTarget) {
        if (invokeTarget == null) return false;
        int count = 0;
        for (int i = 0; i < invokeTarget.length(); i++) {
            if (invokeTarget.charAt(i) == '.') count++;
        }
        return count > 1;
    }

    /**
     * 解析调用目标中的 Bean 名称或类名。
     */
    public static String getBeanName(String invokeTarget) {
        String beanName = invokeTarget.split("\\(")[0];
        int lastDot = beanName.lastIndexOf(".");
        return lastDot == -1 ? beanName : beanName.substring(0, lastDot);
    }

    /**
     * 解析调用目标中的方法名。
     */
    public static String getMethodName(String invokeTarget) {
        String beanName = invokeTarget.split("\\(")[0];
        int lastDot = beanName.lastIndexOf(".");
        return lastDot == -1 ? "" : beanName.substring(lastDot + 1);
    }

    /**
     * 解析方法参数。
     *
     * <p>目前支持 String、Integer、Long、Double、Boolean
     * 五种基础类型。</p>
     * TODO 当前仅支持基础类型，后续可扩展 BigDecimal、Enum、LocalDateTime 等常用类型。
     * TODO 后续可支持 Spring Expression、JSON 参数等更复杂的调用方式。
     */
    public static List<Object[]> getMethodParams(String invokeTarget) {
        int start = invokeTarget.indexOf("(");
        int end = invokeTarget.indexOf(")");
        if (start == -1 || end == -1 || start + 1 == end) {
            return null;
        }
        String methodStr = invokeTarget.substring(start + 1, end);
        String[] methodParams = methodStr.split(",");
        List<Object[]> paramsList = new LinkedList<>();

        // 根据参数内容推断对应的 Java 类型。
        for (String param : methodParams) {
            String str = param.trim();
            if (str.startsWith("'") && str.endsWith("'")) {
                paramsList.add(new Object[]{str.replace("'", ""), String.class});
            } else if ("true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str)) {
                paramsList.add(new Object[]{Boolean.valueOf(str), Boolean.class});
            } else if (str.toUpperCase().contains("L")) {
                paramsList.add(new Object[]{Long.valueOf(str.toUpperCase().replace("L", "")), Long.class});
            } else if (str.toUpperCase().contains("D")) {
                paramsList.add(new Object[]{Double.valueOf(str.toUpperCase().replace("D", "")), Double.class});
            } else {
                paramsList.add(new Object[]{Integer.valueOf(str), Integer.class});
            }
        }
        return paramsList;
    }

    /**
     * 获取方法参数类型数组。
     */
    public static Class<?>[] getMethodParamsType(List<Object[]> methodParams) {
        Class<?>[] types = new Class<?>[methodParams.size()];
        for (int i = 0; i < methodParams.size(); i++) {
            types[i] = (Class<?>) methodParams.get(i)[1];
        }
        return types;
    }

    /**
     * 获取方法参数值数组。
     */
    public static Object[] getMethodParamsValue(List<Object[]> methodParams) {
        Object[] values = new Object[methodParams.size()];
        for (int i = 0; i < methodParams.size(); i++) {
            values[i] = methodParams.get(i)[0];
        }
        return values;
    }
}
package org.paland.quartz.task;

import org.springframework.stereotype.Component;

/**
 * 这是一个普通的业务类
 * 我们的目标是通过数据库配置，让 Quartz 动态反射调用它
 */
@Component("palandTask") // 显式给它起个 Bean 的名字叫 palandTask
public class PalandTask {

    // 1. 无参方法测试
    public void runNoParams() {
        System.out.println(">>> 【PaLand 业务成功触发】执行了无参数业务方法！");
    }

    // 2. 带参数方法测试
    public void runWithParams(String name, Boolean vip, Long count) {
        System.out.println(">>> 【PaLand 业务成功触发】接收到参数 -> 姓名: " + name + ", 是否VIP: " + vip + ", 数量: " + count);
    }
}
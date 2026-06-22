package org.paland.system.module.job.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 仅用于验证Quartz调度链路是否跑通，验证完成后删除
 */
@Slf4j
@Component("demoLogTask")
public class DemoLogTask {

    public void run() {
        log.info("【定时任务验证】DemoLogTask 跳动一次，当前时间：{}", System.currentTimeMillis());
    }
}
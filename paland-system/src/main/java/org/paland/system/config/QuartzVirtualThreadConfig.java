package org.paland.system.config;

import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.Executors;

@Configuration
public class QuartzVirtualThreadConfig {

    @Bean
    public SchedulerFactoryBeanCustomizer customizer() {
        // 让 Quartz 内部的 TaskExecutor 使用 Java 21 的虚拟线程池
        return schedulerFactoryBean -> 
            schedulerFactoryBean.setTaskExecutor(Executors.newVirtualThreadPerTaskExecutor());
    }
}
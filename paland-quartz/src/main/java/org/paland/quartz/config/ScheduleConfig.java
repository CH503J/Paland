package org.paland.quartz.config;

import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * Quartz 调度器配置类。
 *
 * <p>Spring Boot 会自动创建 Quartz 的调度器（Scheduler），
 * 本类用于在自动配置完成后，对调度器进行一些额外的配置和补充，
 * 而不是重新创建一个新的调度器。</p>
 *
 * <p>主要完成以下工作：</p>
 * <ul>
 *     <li>指定 Quartz 使用的数据源，解决 Spring Boot 3 与 Quartz 数据源名称不一致导致的启动问题。</li>
 *     <li>设置调度器的启动行为，例如延迟启动、允许覆盖已存在的任务等。</li>
 *     <li>将 Spring 容器（ApplicationContext）放入 Quartz 上下文中，方便定时任务获取 Spring Bean。</li>
 * </ul>
 *
 * <p>简单来说，这个类就是在 Spring Boot 默认 Quartz 配置的基础上，
 * 根据项目的需要进行统一调整，让 Quartz 能够正常、稳定地运行。</p>
 *
 * @author ChenJun
 */
@Configuration
public class ScheduleConfig {

    @Bean
    public SchedulerFactoryBeanCustomizer schedulerCustomizer() {
        return factoryBean -> {
            // 1. 创建用于补充的属性对象
            Properties prop = new Properties();

            // 2. 核心修复：直接通过 Quartz 的底层参数，强行把数据源别名定义为 "quartzDataSource"
            // Spring Boot 3 默认注入的数据源在 Quartz 内部对应的名字就是 quartzDataSource
            prop.put("org.quartz.jobStore.dataSource", "quartzDataSource");

            // 3. 将这个属性追加到现有的 Quartz 配置中（不会覆盖你在 yml 里写的其他配置）
            factoryBean.setQuartzProperties(prop);

            // 4. 其他自定义行为
            factoryBean.setStartupDelay(1);
            factoryBean.setOverwriteExistingJobs(true);
            factoryBean.setApplicationContextSchedulerContextKey("applicationContextKey");
        };
    }
}

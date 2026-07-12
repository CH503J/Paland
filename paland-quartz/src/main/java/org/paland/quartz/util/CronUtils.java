package org.paland.quartz.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.Date;

/**
 * Cron 表达式工具类。
 *
 * <p>对 Quartz 提供的 {@link CronExpression} 进行简单封装，
 * 提供 Cron 表达式校验及下次执行时间计算等常用功能，
 * 方便业务层统一调用。</p>
 *
 * @author ChenJun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CronUtils {

    /**
     * 校验 Cron 表达式是否合法。
     *
     * @param cronExpression Cron 表达式
     * @return true：合法；false：非法
     */
    public static boolean isValid(String cronExpression) {
        return CronExpression.isValidExpression(cronExpression);
    }

    /**
     * 获取 Cron 表达式的下一次执行时间。
     *
     * @param cronExpression Cron 表达式
     * @return 下一次执行时间
     * @throws IllegalArgumentException Cron 表达式不合法时抛出
     */
    public static Date getNextExecution(String cronExpression) {
        try {
            CronExpression cron = new CronExpression(cronExpression);
            return cron.getNextValidTimeAfter(new Date());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Cron表达式不合法: " + e.getMessage());
        }
    }
}
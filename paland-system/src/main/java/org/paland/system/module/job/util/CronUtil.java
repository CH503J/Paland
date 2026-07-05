package org.paland.system.module.job.util;

import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.Date;

public class CronUtil {

    /**
     * 校验cron表达式是否合法
     */
    public static boolean isInvalid(String cronExpression) {
        return !CronExpression.isValidExpression(cronExpression);
    }

    /**
     * 获取下一次触发时间（如果表达式不合法会抛异常，调用前建议先isValid校验）
     */
    public static Date getNextFireTime(String cronExpression) throws ParseException {
        CronExpression cron = new CronExpression(cronExpression);
        return cron.getNextValidTimeAfter(new Date());
    }
}
package org.paland;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Slf4j
@SpringBootApplication
@Import(cn.hutool.extra.spring.SpringUtil.class)
@MapperScan(value = "org.paland.system.module", annotationClass = Mapper.class)
public class PaLandApplication {

    // 测试 清理超过24小时未使用的缓存、镜像、容器 定时任务
    public static void main(String[] args) {
        SpringApplication.run(PaLandApplication.class, args);
        log.info("===================== PaLand 启动成功 =====================");
    }
}
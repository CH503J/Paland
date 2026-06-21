package org.paland;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
@MapperScan(value = "org.paland.system.module", annotationClass = Mapper.class)
public class PaLandApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaLandApplication.class, args);
        log.info("===================== PaLand 启动成功 =====================");
    }
}
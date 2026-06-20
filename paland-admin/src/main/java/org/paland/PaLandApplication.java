package org.paland;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class PaLandApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaLandApplication.class, args);
        log.info("===================== PaLand 启动成功 =====================");
    }
}
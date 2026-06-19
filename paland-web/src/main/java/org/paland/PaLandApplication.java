package org.paland;

//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。

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
package com.chudahuang.srm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 炊大皇SRM系统启动类
 */
@SpringBootApplication
@MapperScan("com.chudahuang.srm.mapper")
public class SrmApplication {

    public static void main(String[] args) {
        SpringApplication.run(SrmApplication.class, args);
    }
}

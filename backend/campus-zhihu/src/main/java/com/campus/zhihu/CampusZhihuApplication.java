package com.campus.zhihu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.campus.zhihu.mapper")
public class CampusZhihuApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusZhihuApplication.class, args);
    }
}

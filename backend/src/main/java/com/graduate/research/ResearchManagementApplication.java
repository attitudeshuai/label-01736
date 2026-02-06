package com.graduate.research;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.graduate.research.mapper")
public class ResearchManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResearchManagementApplication.class, args);
    }
}

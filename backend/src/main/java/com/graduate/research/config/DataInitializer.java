package com.graduate.research.config;

import com.graduate.research.entity.User;
import com.graduate.research.mapper.UserMapper;
import com.graduate.research.util.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserMapper userMapper;

    // admin123 的 MD5 值
    private static final String DEFAULT_PASSWORD_MD5 = "0192023a7bbd73250516f069df18b500";

    @Override
    public void run(String... args) {
        // 检查是否已有管理员账号
        User admin = userMapper.findByUsername("admin");
        if (admin == null) {
            log.info("初始化管理员账号...");
            
            // 创建管理员
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(DEFAULT_PASSWORD_MD5);
            adminUser.setRealName("系统管理员");
            adminUser.setEmail("admin@example.com");
            adminUser.setRole(1);
            adminUser.setStatus(1);
            userMapper.insert(adminUser);
            
            // 创建导师
            User teacher = new User();
            teacher.setUsername("teacher01");
            teacher.setPassword(DEFAULT_PASSWORD_MD5);
            teacher.setRealName("张教授");
            teacher.setEmail("zhang@example.com");
            teacher.setRole(2);
            teacher.setResearchDirection("人工智能与机器学习");
            teacher.setStatus(1);
            userMapper.insert(teacher);
            
            // 创建学生
            User student = new User();
            student.setUsername("student01");
            student.setPassword(DEFAULT_PASSWORD_MD5);
            student.setRealName("李同学");
            student.setEmail("li@example.com");
            student.setRole(3);
            student.setSupervisorId(teacher.getId());
            student.setStudentId("2024001");
            student.setResearchDirection("深度学习");
            student.setStatus(1);
            userMapper.insert(student);
            
            log.info("初始化账号完成！");
            log.info("管理员: admin / admin123");
            log.info("导师: teacher01 / admin123");
            log.info("学生: student01 / admin123");
        }
    }
}

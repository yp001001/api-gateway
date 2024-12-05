package org.imooc.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author: yp
 * @date: 2024/12/3 14:40
 * @description:
 */
@SpringBootApplication
@EnableScheduling
public class UserApplication2 {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication2.class);
    }
}


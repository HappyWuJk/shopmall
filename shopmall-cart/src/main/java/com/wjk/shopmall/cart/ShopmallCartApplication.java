package com.wjk.shopmall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


@EnableFeignClients
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class ShopmallCartApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopmallCartApplication.class, args);
    }

}

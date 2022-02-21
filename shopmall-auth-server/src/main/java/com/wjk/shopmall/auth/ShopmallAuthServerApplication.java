package com.wjk.shopmall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 *   SpringSession核心原理
 *     1）@EnableRedisHttpSession 导入RedisHttpSessionConfiguration配置
 *         给容器添加了一个组件
 *           RedisOperationsSessionRepository  redis操作session  session的增删改查封装类
 *         SessionRepositoryFilter  session存储过滤器，每个请求过来都必须经过filter
 *         1. 创建的时候，就自动从容器中获取到了SessionRepository
 *         2. 原始的request、response都被包装，SessionRepositoryRequestWrapper，SessionRepositoryResponseWrapper
 *         3. 以后获取session。request.getSession();
 *         4. 其实调用的是wrappedRequest.getSession() ====> SessionRepository中获取到
 *
 *     装饰着模式
 *     自动延期  redis中的数据也是有过期时间的
 * */

@EnableRedisHttpSession  // 整合Redis作为session存储
@EnableFeignClients
@SpringBootApplication
public class ShopmallAuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopmallAuthServerApplication.class, args);
    }

}

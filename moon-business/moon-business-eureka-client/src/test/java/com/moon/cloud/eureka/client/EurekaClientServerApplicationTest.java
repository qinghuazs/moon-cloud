package com.moon.cloud.eureka.client;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Eureka客户端服务端应用测试类
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@SpringBootTest
@TestPropertySource(properties = {
    "eureka.client.register-with-eureka=false",
    "eureka.client.fetch-registry=false",
    "eureka.server.enable-self-preservation=false"
})
class EurekaClientServerApplicationTest {

    /**
     * 测试应用上下文加载
     */
    @Test
    void contextLoads() {
        // 测试Spring Boot应用上下文是否能够正常加载
        // 如果上下文加载失败，测试将抛出异常
    }
}
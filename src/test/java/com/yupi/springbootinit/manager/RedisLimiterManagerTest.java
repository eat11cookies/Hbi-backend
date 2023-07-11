package com.yupi.springbootinit.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class RedisLimiterManagerTest {

    @Resource
//    private RedisLimiterManager redisLimiterManager;


    @Test
    void doRateLimit() {
        for (int i = 0; i < 5; i++) {
//            redisLimiterManager.doRateLimit("1");
            System.out.println("成功");
        }

    }
}
package com.samhap.kokomen.global;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
public abstract class BaseTest {

    @MockitoSpyBean
    protected RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private MySQLDatabaseCleaner mySQLDatabaseCleaner;

    @BeforeEach
    void baseTestSetUp() {
        mySQLDatabaseCleaner.executeTruncate();
    }
}

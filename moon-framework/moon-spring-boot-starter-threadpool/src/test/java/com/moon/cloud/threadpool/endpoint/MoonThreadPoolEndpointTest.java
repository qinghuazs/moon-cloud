package com.moon.cloud.threadpool.endpoint;

import com.moon.cloud.threadpool.endpoint.dto.ThreadPoolInfoDTO;
import com.moon.cloud.threadpool.factory.MoonThreadPoolFactory;
import com.moon.cloud.threadpool.registry.ThreadPoolRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Moon Thread Pool Endpoint 测试类
 * 
 * @author moon
 * @since 1.0.0
 */
@SpringBootTest(classes = {TestConfig.class})
@TestPropertySource(properties = {
    "management.endpoint.threadpools.enabled=true",
    "management.endpoints.web.exposure.include=threadpools"
})
class MoonThreadPoolEndpointTest {

    private MoonThreadPoolEndpoint endpoint;
    private ThreadPoolExecutor testPool;
    
    @BeforeEach
    void setUp() {
        endpoint = new MoonThreadPoolEndpoint();
        
        // 创建测试线程池
        testPool = (ThreadPoolExecutor) MoonThreadPoolFactory.createCustomThreadPoolWithRetry(
            2,
            4,
            60,
            10,
            "test-pool",
                null
        );
    }
    
    @Test
    void testGetAllThreadPools() {
        // 测试获取所有线程池
        Map<String, Object> result = endpoint.threadPools();
        
        assertNotNull(result);
        assertTrue(result.containsKey("total"));
        assertTrue(result.containsKey("pools"));
        
        Integer total = (Integer) result.get("total");
        assertTrue(total >= 1); // 至少包含我们创建的测试线程池
    }
    
    @Test
    void testGetSpecificThreadPool() {
        // 测试获取特定线程池
        Map<String, Object> result = endpoint.threadPool("test-pool");
        
        assertNotNull(result);
        assertTrue(result.containsKey("info"));
        
        @SuppressWarnings("unchecked")
        ThreadPoolInfoDTO info = (ThreadPoolInfoDTO) result.get("info");
        assertEquals("test-pool", info.getPoolName());
//        assertEquals(2, info.get("corePoolSize"));
//        assertEquals(4, info.get("maximumPoolSize"));
    }
    
    @Test
    void testGetNonExistentThreadPool() {
        // 测试获取不存在的线程池
        Map<String, Object> result = endpoint.threadPool("non-existent-pool");
        
        assertNotNull(result);
        assertTrue(result.containsKey("msg"));
        assertTrue(result.get("msg").toString().contains("不存在"));
    }
    
    @Test
    void testAdjustThreadPool() {
        // 测试调整线程池参数
        Map<String, Object> result = endpoint.adjustThreadPool("test-pool", 3, 6);
        
        assertNotNull(result);
        assertEquals("test-pool", result.get("poolName"));
//        assertEquals(2, result.get("oldCorePoolSize"));
//        assertEquals(3, result.get("newCorePoolSize"));
//        assertEquals(4, result.get("oldMaximumPoolSize"));
//        assertEquals(6, result.get("newMaximumPoolSize"));
        
        // 验证线程池参数确实被修改了
        assertEquals(3, testPool.getCorePoolSize());
        assertEquals(6, testPool.getMaximumPoolSize());
    }
    
    @Test
    void testAdjustNonExistentThreadPool() {
        // 测试调整不存在的线程池
        Map<String, Object> result = endpoint.adjustThreadPool("non-existent-pool", 3, 6);
        
        assertNotNull(result);
        assertTrue(result.containsKey("msg"));
        assertTrue(result.get("msg").toString().contains("不存在"));
    }
    
    @Test
    void testAdjustThreadPoolWithInvalidParams() {
        // 测试使用无效参数调整线程池
        Map<String, Object> result = endpoint.adjustThreadPool("test-pool", 0, 6);
        
        assertNotNull(result);
        assertTrue(result.containsKey("msg"));
        assertTrue(result.get("msg").toString().contains("必须大于0"));
    }

}
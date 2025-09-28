package com.moon.cloud.appstore.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.appstore.dto.FreeAppQueryDTO;
import com.moon.cloud.appstore.entity.App;
import com.moon.cloud.appstore.entity.FreePromotion;
import com.moon.cloud.appstore.mapper.AppMapper;
import com.moon.cloud.appstore.mapper.FreePromotionMapper;
import com.moon.cloud.appstore.vo.FreeAppStatisticsVO;
import com.moon.cloud.appstore.vo.FreePromotionVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 限免推广服务测试类
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("限免推广服务测试")
class FreePromotionServiceImplTest {

    @Mock
    private FreePromotionMapper freePromotionMapper;

    @Mock
    private AppMapper appMapper;

    @InjectMocks
    private FreePromotionServiceImpl freePromotionService;

    private FreePromotion testPromotion;
    private App testApp;
    private FreeAppQueryDTO queryDTO;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testPromotion = createTestPromotion();
        testApp = createTestApp();
        queryDTO = createTestQueryDTO();
    }

    @Test
    @DisplayName("测试获取今日限免列表")
    void testGetTodayFreeApps() {
        // 准备数据
        Page<FreePromotion> mockPage = new Page<>(1, 20);
        List<FreePromotion> promotions = Arrays.asList(testPromotion);
        mockPage.setRecords(promotions);
        mockPage.setTotal(1);

        // Mock行为
        when(freePromotionMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);
        when(appMapper.selectById(anyString())).thenReturn(testApp);

        // 执行测试
        Page<FreePromotionVO> result = freePromotionService.getTodayFreeApps(queryDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertFalse(result.getRecords().isEmpty());
        assertEquals("测试应用", result.getRecords().get(0).getAppName());

        // 验证方法调用
        verify(freePromotionMapper, times(1)).selectPage(any(Page.class), any());
        verify(appMapper, times(1)).selectById(anyString());
    }

    @Test
    @DisplayName("测试获取即将结束的限免应用")
    void testGetEndingSoonApps() {
        // 准备数据
        testPromotion.setEndTime(LocalDateTime.now().plusHours(3));
        List<FreePromotion> promotions = Arrays.asList(testPromotion);

        // Mock行为
        when(freePromotionMapper.selectList(any())).thenReturn(promotions);
        when(appMapper.selectById(anyString())).thenReturn(testApp);

        // 执行测试
        List<FreePromotionVO> result = freePromotionService.getEndingSoonApps(6);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsEndingSoon());

        // 验证方法调用
        verify(freePromotionMapper, times(1)).selectList(any());
    }

    @Test
    @DisplayName("测试获取热门限免应用")
    void testGetHotFreeApps() {
        // 准备数据
        testPromotion.setViewCount(1000);
        testPromotion.setClickCount(500);
        List<FreePromotion> promotions = Arrays.asList(testPromotion);

        // Mock行为
        when(freePromotionMapper.selectList(any())).thenReturn(promotions);
        when(appMapper.selectById(anyString())).thenReturn(testApp);

        // 执行测试
        List<FreePromotionVO> result = freePromotionService.getHotFreeApps(10);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsHot());
        assertTrue(result.get(0).getHotScore() > 0);

        // 验证方法调用
        verify(freePromotionMapper, times(1)).selectList(any());
    }

    @Test
    @DisplayName("测试获取限免统计信息")
    void testGetFreeAppStatistics() {
        // 准备数据
        List<FreePromotion> todayPromotions = Arrays.asList(testPromotion);
        List<FreePromotion> activePromotions = Arrays.asList(testPromotion);

        // Mock行为
        when(freePromotionMapper.selectList(any())).thenReturn(todayPromotions, activePromotions);
        when(freePromotionMapper.selectCount(any())).thenReturn(5L);
        when(appMapper.selectById(anyString())).thenReturn(testApp);

        // 执行测试
        FreeAppStatisticsVO result = freePromotionService.getFreeAppStatistics();

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTodayFreeCount());
        assertEquals(1, result.getActiveFreeCount());
        assertNotNull(result.getTodayTotalSavings());
        assertTrue(result.getTodayTotalSavings().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("测试更新限免状态")
    void testUpdatePromotionStatus() {
        // 准备数据
        testPromotion.setEndTime(LocalDateTime.now().minusHours(1)); // 已过期
        List<FreePromotion> activePromotions = Arrays.asList(testPromotion);

        // Mock行为
        when(freePromotionMapper.selectList(any())).thenReturn(activePromotions);
        when(appMapper.selectById(anyString())).thenReturn(testApp);
        when(freePromotionMapper.updateById(any())).thenReturn(1);

        // 执行测试
        freePromotionService.updatePromotionStatus();

        // 验证方法调用
        verify(freePromotionMapper, times(1)).selectList(any());
        verify(freePromotionMapper, times(1)).updateById(any());
    }

    @Test
    @DisplayName("测试检测新限免应用")
    void testDetectNewPromotions() {
        // 准备数据
        testApp.setCurrentPrice(BigDecimal.ZERO);
        testApp.setOriginalPrice(new BigDecimal("18.00"));
        List<App> apps = Arrays.asList(testApp);

        // Mock行为
        when(appMapper.selectList(any())).thenReturn(apps);
        when(freePromotionMapper.selectOne(any())).thenReturn(null); // 没有现有记录
        when(freePromotionMapper.insert(any())).thenReturn(1);

        // 执行测试
        int count = freePromotionService.detectNewPromotions();

        // 验证结果
        assertEquals(1, count);

        // 验证方法调用
        verify(appMapper, times(1)).selectList(any());
        verify(freePromotionMapper, times(1)).insert(any());
    }

    @Test
    @DisplayName("测试按分类获取限免应用")
    void testGetFreeAppsByCategory() {
        // 准备数据
        Page<FreePromotion> mockPage = new Page<>(1, 20);
        mockPage.setRecords(Arrays.asList(testPromotion));
        mockPage.setTotal(1);

        // Mock行为
        when(freePromotionMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);
        when(appMapper.selectById(anyString())).thenReturn(testApp);

        // 执行测试
        Page<FreePromotionVO> result = freePromotionService.getFreeAppsByCategory("6014", 1, 20);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getRecords());

        // 验证方法调用
        verify(freePromotionMapper, times(1)).selectPage(any(Page.class), any());
    }

    @Test
    @DisplayName("测试获取应用限免历史")
    void testGetPromotionHistory() {
        // 准备数据
        List<FreePromotion> history = Arrays.asList(testPromotion, testPromotion);

        // Mock行为
        when(freePromotionMapper.selectList(any())).thenReturn(history);
        when(appMapper.selectById(anyString())).thenReturn(testApp);

        // 执行测试
        List<FreePromotionVO> result = freePromotionService.getPromotionHistory("123456");

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());

        // 验证方法调用
        verify(freePromotionMapper, times(1)).selectList(any());
    }

    @Test
    @DisplayName("测试标记为已查看")
    void testMarkAsViewed() {
        // Mock行为
        when(freePromotionMapper.update(isNull(), any())).thenReturn(1);

        // 执行测试
        boolean result = freePromotionService.markAsViewed("1");

        // 验证结果
        assertTrue(result);

        // 验证方法调用
        verify(freePromotionMapper, times(1)).update(isNull(), any());
    }

    @Test
    @DisplayName("测试增加点击次数")
    void testIncreaseClickCount() {
        // Mock行为
        when(freePromotionMapper.update(isNull(), any())).thenReturn(1);

        // 执行测试
        boolean result = freePromotionService.increaseClickCount("1");

        // 验证结果
        assertTrue(result);

        // 验证方法调用
        verify(freePromotionMapper, times(1)).update(isNull(), any());
    }

    @Test
    @DisplayName("测试查询条件构建")
    void testBuildQueryWithFilters() {
        // 准备带筛选条件的查询
        queryDTO.setPromotionType("FREE");
        queryDTO.setMinOriginalPrice(new BigDecimal("10"));
        queryDTO.setMaxOriginalPrice(new BigDecimal("100"));
        queryDTO.setOnlyNew(true);
        queryDTO.setOnlyEndingSoon(false);

        Page<FreePromotion> mockPage = new Page<>(1, 20);
        mockPage.setRecords(new ArrayList<>());

        // Mock行为
        when(freePromotionMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        // 执行测试
        Page<FreePromotionVO> result = freePromotionService.getTodayFreeApps(queryDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getTotal());

        // 验证方法调用
        verify(freePromotionMapper, times(1)).selectPage(any(Page.class), any());
    }

    // ========== 辅助方法 ==========

    private FreePromotion createTestPromotion() {
        FreePromotion promotion = new FreePromotion();
        promotion.setId("1");
        promotion.setAppId("app1");
        promotion.setAppstoreAppId("123456");
        promotion.setPromotionType("FREE");
        promotion.setOriginalPrice(new BigDecimal("18.00"));
        promotion.setPromotionPrice(BigDecimal.ZERO);
        promotion.setDiscountRate(new BigDecimal("100"));
        promotion.setSavingsAmount(new BigDecimal("18.00"));
        promotion.setStartTime(LocalDateTime.now().minusHours(2));
        promotion.setDiscoveredAt(LocalDateTime.now().minusHours(2));
        promotion.setStatus("ACTIVE");
        promotion.setViewCount(100);
        promotion.setClickCount(50);
        promotion.setShareCount(10);
        promotion.setIsHot(false);
        promotion.setIsFeatured(false);
        promotion.setPriorityScore(80);
        return promotion;
    }

    private App createTestApp() {
        App app = new App();
        app.setId("app1");
        app.setAppId("123456");
        app.setName("测试应用");
        app.setBundleId("com.test.app");
        app.setIconUrl("https://example.com/icon.png");
        app.setDeveloperName("测试开发者");
        app.setPrimaryCategoryId("6014");
        app.setPrimaryCategoryName("游戏");
        app.setRating(new BigDecimal("4.5"));
        app.setRatingCount(1000);
        app.setFileSize(100000000L);
        app.setVersion("1.0.0");
        app.setDescription("这是一个测试应用");
        app.setCurrentPrice(BigDecimal.ZERO);
        app.setOriginalPrice(new BigDecimal("18.00"));
        app.setScreenshots(Arrays.asList("https://example.com/screenshot1.png"));
        app.setSupportedDevices(Arrays.asList("iPhone", "iPad"));
        app.setLanguages(Arrays.asList("ZH", "EN"));
        app.setContentRating("4+");
        return app;
    }

    private FreeAppQueryDTO createTestQueryDTO() {
        FreeAppQueryDTO dto = new FreeAppQueryDTO();
        dto.setPage(1);
        dto.setSize(20);
        dto.setStatus("ACTIVE");
        dto.setSortBy("time");
        dto.setSortOrder("desc");
        return dto;
    }
}
package com.moon.cloud.appstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moon.cloud.appstore.entity.App;
import com.moon.cloud.appstore.entity.AppPriceHistory;
import com.moon.cloud.appstore.entity.FreePromotion;
import com.moon.cloud.appstore.mapper.AppMapper;
import com.moon.cloud.appstore.mapper.AppPriceHistoryMapper;
import com.moon.cloud.appstore.mapper.FreePromotionMapper;
import com.moon.cloud.appstore.vo.AppDetailVO;
import com.moon.cloud.appstore.vo.AppPriceChartVO;
import com.moon.cloud.appstore.vo.AppSimilarVO;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 应用详情服务实现类单元测试
 *
 * @author Moon Cloud
 * @since 2024-09-28
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("应用详情服务测试")
class AppDetailServiceImplTest {

    @Mock
    private AppMapper appMapper;

    @Mock
    private AppPriceHistoryMapper appPriceHistoryMapper;

    @Mock
    private FreePromotionMapper freePromotionMapper;

    @InjectMocks
    private AppDetailServiceImpl appDetailService;

    private App testApp;
    private String testAppId;
    private FreePromotion testPromotion;
    private List<AppPriceHistory> testPriceHistory;

    @BeforeEach
    void setUp() {
        testAppId = "284882215";

        // 准备测试应用数据
        testApp = new App();
        testApp.setId("1");
        testApp.setAppId(testAppId);
        testApp.setBundleId("com.facebook.Facebook");
        testApp.setName("Facebook");
        testApp.setDescription("Connect with friends and the world around you on Facebook.");
        testApp.setDeveloperName("Meta Platforms, Inc.");
        testApp.setDeveloperId("389801252");
        testApp.setVersion("434.0");
        testApp.setRating(new BigDecimal("3.8"));
        testApp.setRatingCount(11234567);
        testApp.setCurrentPrice(BigDecimal.ZERO);
        testApp.setOriginalPrice(BigDecimal.ZERO);
        testApp.setIsFree(true);
        testApp.setPrimaryCategoryId("6005");
        testApp.setPrimaryCategoryName("Social Networking");
        testApp.setFileSize(314572800L); // 300MB
        testApp.setContentRating("12+");
        testApp.setMinimumOsVersion("12.0");
        testApp.setReleaseDate(LocalDateTime.of(2008, 7, 10, 0, 0));
        testApp.setUpdatedDate(LocalDateTime.now().minusDays(3));

        // 准备限免数据
        testPromotion = new FreePromotion();
        testPromotion.setId("1");
        testPromotion.setAppstoreAppId(testAppId);
        testPromotion.setStartTime(LocalDateTime.now().minusHours(12));
        testPromotion.setEndTime(LocalDateTime.now().plusHours(36));
        testPromotion.setSavingsAmount(new BigDecimal("68.00"));
        testPromotion.setStatus("ACTIVE");

        // 准备价格历史数据
        testPriceHistory = new ArrayList<>();
        AppPriceHistory history1 = new AppPriceHistory();
        history1.setAppId(testAppId);
        history1.setOldPrice(new BigDecimal("68.00"));
        history1.setNewPrice(BigDecimal.ZERO);
        history1.setChangeType("FREE");
        history1.setChangeTime(LocalDateTime.now().minusHours(12));
        history1.setPriceChange(new BigDecimal("-68.00"));
        history1.setChangePercent(new BigDecimal("-100"));
        testPriceHistory.add(history1);
    }

    @Test
    @DisplayName("获取应用详情 - 成功获取带限免信息")
    void testGetAppDetail_WithPromotion() {
        // Given
        when(appMapper.selectById(testAppId)).thenReturn(null);
        when(appMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testApp);
        when(freePromotionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testPromotion);
        when(appPriceHistoryMapper.getPriceHistoryByTimeRange(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(testPriceHistory);

        // When
        AppDetailVO result = appDetailService.getAppDetail(testAppId);

        // Then
        assertNotNull(result);
        assertEquals(testApp.getId(), result.getAppId());
        assertEquals(testAppId, result.getAppstoreId());
        assertEquals("Facebook", result.getName());
        assertEquals("Meta Platforms, Inc.", result.getDeveloperName());
        assertTrue(result.getIsFreeNow());
        assertNotNull(result.getFreePromotion());
        assertNotNull(result.getPriceHistory());
        assertEquals("300.0 MB", result.getFileSizeFormatted());

        // Verify
        verify(appMapper, times(1)).selectById(testAppId);
        verify(appMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
        verify(freePromotionMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取应用详情 - 应用不存在")
    void testGetAppDetail_NotFound() {
        // Given
        when(appMapper.selectById(testAppId)).thenReturn(null);
        when(appMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // When
        AppDetailVO result = appDetailService.getAppDetail(testAppId);

        // Then
        assertNull(result);

        // Verify
        verify(appMapper, times(1)).selectById(testAppId);
        verify(appMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
        verify(freePromotionMapper, never()).selectOne(any());
    }

    @Test
    @DisplayName("获取价格图表数据")
    void testGetAppPriceChart() {
        // Given
        when(appMapper.selectById(testAppId)).thenReturn(null);
        when(appMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testApp);
        when(appPriceHistoryMapper.getPriceHistoryByTimeRange(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(testPriceHistory);

        // When
        AppPriceChartVO result = appDetailService.getAppPriceChart(testAppId, 90);

        // Then
        assertNotNull(result);
        assertEquals(testAppId, result.getAppId());
        assertEquals("Facebook", result.getAppName());
        assertEquals(90, result.getDays());
        assertEquals(BigDecimal.ZERO, result.getCurrentPrice());
        assertEquals(BigDecimal.ZERO, result.getLowestPrice());
        assertEquals(1, result.getChangeCount());
        assertEquals(1, result.getFreeCount());
        assertNotNull(result.getPricePoints());
        assertNotNull(result.getPriceEvents());

        // Verify
        verify(appPriceHistoryMapper, times(1)).getPriceHistoryByTimeRange(
                eq(testAppId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("获取价格图表数据 - 使用默认天数")
    void testGetAppPriceChart_DefaultDays() {
        // Given
        when(appMapper.selectById(testAppId)).thenReturn(null);
        when(appMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testApp);
        when(appPriceHistoryMapper.getPriceHistoryByTimeRange(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // When
        AppPriceChartVO result = appDetailService.getAppPriceChart(testAppId, null);

        // Then
        assertNotNull(result);
        assertEquals(90, result.getDays()); // 默认90天
    }

    @Test
    @DisplayName("获取相似应用")
    void testGetSimilarApps() {
        // Given
        List<App> similarApps = new ArrayList<>();
        App similar1 = new App();
        similar1.setId("2");
        similar1.setAppId("447188370");
        similar1.setName("Snapchat");
        similar1.setDeveloperName("Snap, Inc.");
        similar1.setRating(new BigDecimal("4.0"));
        similar1.setCurrentPrice(BigDecimal.ZERO);
        similar1.setPrimaryCategoryName("Social Networking");
        similarApps.add(similar1);

        when(appMapper.selectById(testAppId)).thenReturn(null);
        when(appMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testApp);
        when(appMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(similarApps);

        // When
        List<AppSimilarVO> result = appDetailService.getSimilarApps(testAppId, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Snapchat", result.get(0).getName());
        assertEquals("同分类高评分应用", result.get(0).getRecommendReason());

        // Verify
        verify(appMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取同开发商应用")
    void testGetDeveloperApps() {
        // Given
        List<App> developerApps = new ArrayList<>();
        App app1 = new App();
        app1.setId("2");
        app1.setAppId("454638411");
        app1.setName("Messenger");
        app1.setDeveloperName("Meta Platforms, Inc.");
        app1.setRating(new BigDecimal("3.5"));
        developerApps.add(app1);

        when(appMapper.selectById(testAppId)).thenReturn(null);
        when(appMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testApp);
        when(appMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(developerApps);

        // When
        List<AppSimilarVO> result = appDetailService.getDeveloperApps(testAppId, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Messenger", result.get(0).getName());
        assertEquals("同一开发商", result.get(0).getRecommendReason());

        // Verify
        verify(appMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取分类热门应用")
    void testGetCategoryTopApps() {
        // Given
        List<App> topApps = new ArrayList<>();
        App app1 = new App();
        app1.setId("2");
        app1.setAppId("333903271");
        app1.setName("Twitter");
        app1.setDeveloperName("X Corp.");
        app1.setRating(new BigDecimal("4.2"));
        app1.setRatingCount(1000000);
        topApps.add(app1);

        when(appMapper.selectById(testAppId)).thenReturn(null);
        when(appMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testApp);
        when(appMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(topApps);

        // When
        List<AppSimilarVO> result = appDetailService.getCategoryTopApps(testAppId, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Twitter", result.get(0).getName());
        assertEquals("分类热门应用", result.get(0).getRecommendReason());

        // Verify
        verify(appMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("增加查看次数")
    void testIncreaseViewCount() {
        // Given
        when(appMapper.selectById(testAppId)).thenReturn(testApp);

        // When
        boolean result = appDetailService.increaseViewCount(testAppId);

        // Then
        assertTrue(result);

        // Verify
        verify(appMapper, times(1)).selectById(testAppId);
    }

    @Test
    @DisplayName("记录下载")
    void testRecordDownload() {
        // Given
        when(appMapper.selectById(testAppId)).thenReturn(testApp);

        // When
        boolean result = appDetailService.recordDownload(testAppId);

        // Then
        assertTrue(result);

        // Verify
        verify(appMapper, times(1)).selectById(testAppId);
    }

    @Test
    @DisplayName("获取App Store链接")
    void testGetAppStoreUrl() {
        // Given
        when(appMapper.selectById(testAppId)).thenReturn(null);
        when(appMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testApp);

        // When
        String result = appDetailService.getAppStoreUrl(testAppId);

        // Then
        assertNotNull(result);
        assertEquals("https://apps.apple.com/cn/app/id" + testAppId, result);

        // Verify
        verify(appMapper, times(1)).selectById(testAppId);
        verify(appMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取App Store链接 - 应用不存在")
    void testGetAppStoreUrl_NotFound() {
        // Given
        when(appMapper.selectById(testAppId)).thenReturn(null);
        when(appMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // When
        String result = appDetailService.getAppStoreUrl(testAppId);

        // Then
        assertNull(result);

        // Verify
        verify(appMapper, times(1)).selectById(testAppId);
        verify(appMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("文件大小格式化")
    void testFormatFileSize() {
        // Given
        App smallApp = new App();
        smallApp.setFileSize(1024L); // 1KB

        App mediumApp = new App();
        mediumApp.setFileSize(5242880L); // 5MB

        App largeApp = new App();
        largeApp.setFileSize(1073741824L); // 1GB

        when(appMapper.selectById("small")).thenReturn(smallApp);
        when(appMapper.selectById("medium")).thenReturn(mediumApp);
        when(appMapper.selectById("large")).thenReturn(largeApp);
        when(freePromotionMapper.selectOne(any())).thenReturn(null);
        when(appPriceHistoryMapper.getPriceHistoryByTimeRange(any(), any(), any())).thenReturn(new ArrayList<>());

        // When & Then
        AppDetailVO smallResult = appDetailService.getAppDetail("small");
        assertEquals("1.0 KB", smallResult.getFileSizeFormatted());

        AppDetailVO mediumResult = appDetailService.getAppDetail("medium");
        assertEquals("5.0 MB", mediumResult.getFileSizeFormatted());

        AppDetailVO largeResult = appDetailService.getAppDetail("large");
        assertEquals("1.0 GB", largeResult.getFileSizeFormatted());
    }
}
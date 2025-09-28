package com.moon.cloud.appstore.controller;

import com.moon.cloud.appstore.service.AppDetailService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 应用详情控制器单元测试
 *
 * @author Moon Cloud
 * @since 2024-09-28
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("应用详情控制器测试")
class AppDetailControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AppDetailService appDetailService;

    @InjectMocks
    private AppDetailController appDetailController;

    private String testAppId;
    private AppDetailVO testAppDetail;
    private AppPriceChartVO testPriceChart;
    private List<AppSimilarVO> testSimilarApps;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(appDetailController).build();

        // 初始化测试数据
        testAppId = "284882215";

        // 准备测试应用详情
        testAppDetail = new AppDetailVO();
        testAppDetail.setAppId("1");
        testAppDetail.setAppstoreId(testAppId);
        testAppDetail.setName("Facebook");
        testAppDetail.setDescription("Connect with friends and the world");
        testAppDetail.setDeveloperName("Meta Platforms, Inc.");
        testAppDetail.setVersion("434.0");
        testAppDetail.setRating(new BigDecimal("3.8"));
        testAppDetail.setRatingCount(11234567);
        testAppDetail.setCurrentPrice(BigDecimal.ZERO);
        testAppDetail.setOriginalPrice(BigDecimal.ZERO);
        testAppDetail.setIsFreeNow(true);

        // 准备价格图表数据
        testPriceChart = new AppPriceChartVO();
        testPriceChart.setAppId(testAppId);
        testPriceChart.setAppName("Facebook");
        testPriceChart.setDays(90);
        testPriceChart.setCurrentPrice(BigDecimal.ZERO);
        testPriceChart.setLowestPrice(BigDecimal.ZERO);
        testPriceChart.setHighestPrice(BigDecimal.ZERO);
        testPriceChart.setAveragePrice(BigDecimal.ZERO);
        testPriceChart.setChangeCount(0);
        testPriceChart.setFreeCount(0);

        // 准备相似应用数据
        testSimilarApps = new ArrayList<>();
        AppSimilarVO similar1 = new AppSimilarVO();
        similar1.setId("2");
        similar1.setAppId("447188370");
        similar1.setName("Snapchat");
        similar1.setDeveloperName("Snap, Inc.");
        similar1.setRating(new BigDecimal("4.0"));
        similar1.setCurrentPrice(BigDecimal.ZERO);
        similar1.setRecommendReason("同分类高评分应用");
        testSimilarApps.add(similar1);

        AppSimilarVO similar2 = new AppSimilarVO();
        similar2.setId("3");
        similar2.setAppId("310633997");
        similar2.setName("WhatsApp Messenger");
        similar2.setDeveloperName("WhatsApp Inc.");
        similar2.setRating(new BigDecimal("4.5"));
        similar2.setCurrentPrice(BigDecimal.ZERO);
        similar2.setRecommendReason("同分类高评分应用");
        testSimilarApps.add(similar2);
    }

    @Test
    @DisplayName("获取应用详情 - 应用存在")
    void testGetAppDetail_Success() throws Exception {
        // Given
        when(appDetailService.getAppDetail(testAppId)).thenReturn(testAppDetail);
        when(appDetailService.increaseViewCount(testAppId)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/appstore/app/{appId}", testAppId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.appstoreId").value(testAppId))
                .andExpect(jsonPath("$.data.name").value("Facebook"))
                .andExpect(jsonPath("$.data.developerName").value("Meta Platforms, Inc."))
                .andExpect(jsonPath("$.data.rating").value(3.8))
                .andExpect(jsonPath("$.data.isFreeNow").value(true));

        // Verify
        verify(appDetailService, times(1)).getAppDetail(testAppId);
        verify(appDetailService, times(1)).increaseViewCount(testAppId);
    }

    @Test
    @DisplayName("获取应用详情 - 应用不存在")
    void testGetAppDetail_NotFound() throws Exception {
        // Given
        when(appDetailService.getAppDetail(testAppId)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/appstore/app/{appId}", testAppId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("应用不存在"))
                .andExpect(jsonPath("$.appId").value(testAppId));

        // Verify
        verify(appDetailService, times(1)).getAppDetail(testAppId);
        verify(appDetailService, never()).increaseViewCount(anyString());
    }

    @Test
    @DisplayName("获取价格历史图表数据")
    void testGetAppPriceChart() throws Exception {
        // Given
        when(appDetailService.getAppPriceChart(testAppId, 90)).thenReturn(testPriceChart);

        // When & Then
        mockMvc.perform(get("/api/appstore/app/{appId}/price-chart", testAppId)
                        .param("days", "90"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.appId").value(testAppId))
                .andExpect(jsonPath("$.data.appName").value("Facebook"))
                .andExpect(jsonPath("$.data.days").value(90));

        // Verify
        verify(appDetailService, times(1)).getAppPriceChart(testAppId, 90);
    }

    @Test
    @DisplayName("获取价格历史图表数据 - 限制最大天数")
    void testGetAppPriceChart_MaxDays() throws Exception {
        // Given
        when(appDetailService.getAppPriceChart(testAppId, 365)).thenReturn(testPriceChart);

        // When & Then
        mockMvc.perform(get("/api/appstore/app/{appId}/price-chart", testAppId)
                        .param("days", "500"))
                .andDo(print())
                .andExpect(status().isOk());

        // Verify - 应该调用时限制为365天
        verify(appDetailService, times(1)).getAppPriceChart(testAppId, 365);
    }

    @Test
    @DisplayName("获取相似应用")
    void testGetSimilarApps() throws Exception {
        // Given
        when(appDetailService.getSimilarApps(testAppId, 10)).thenReturn(testSimilarApps);

        // When & Then
        mockMvc.perform(get("/api/appstore/app/{appId}/similar", testAppId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.appId").value(testAppId))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("Snapchat"))
                .andExpect(jsonPath("$.data[1].name").value("WhatsApp Messenger"));

        // Verify
        verify(appDetailService, times(1)).getSimilarApps(testAppId, 10);
    }

    @Test
    @DisplayName("获取相似应用 - 限制最大数量")
    void testGetSimilarApps_MaxLimit() throws Exception {
        // Given
        when(appDetailService.getSimilarApps(testAppId, 50)).thenReturn(testSimilarApps);

        // When & Then
        mockMvc.perform(get("/api/appstore/app/{appId}/similar", testAppId)
                        .param("limit", "100"))
                .andDo(print())
                .andExpect(status().isOk());

        // Verify - 应该调用时限制为50
        verify(appDetailService, times(1)).getSimilarApps(testAppId, 50);
    }

    @Test
    @DisplayName("获取同开发商应用")
    void testGetDeveloperApps() throws Exception {
        // Given
        when(appDetailService.getDeveloperApps(testAppId, 10)).thenReturn(testSimilarApps);

        // When & Then
        mockMvc.perform(get("/api/appstore/app/{appId}/developer-apps", testAppId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.appId").value(testAppId))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.data").isArray());

        // Verify
        verify(appDetailService, times(1)).getDeveloperApps(testAppId, 10);
    }

    @Test
    @DisplayName("获取同分类热门应用")
    void testGetCategoryTopApps() throws Exception {
        // Given
        when(appDetailService.getCategoryTopApps(testAppId, 10)).thenReturn(testSimilarApps);

        // When & Then
        mockMvc.perform(get("/api/appstore/app/{appId}/category-top", testAppId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.appId").value(testAppId))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.data").isArray());

        // Verify
        verify(appDetailService, times(1)).getCategoryTopApps(testAppId, 10);
    }

    @Test
    @DisplayName("获取所有相关应用")
    void testGetAllRelatedApps() throws Exception {
        // Given
        when(appDetailService.getSimilarApps(testAppId, 5)).thenReturn(testSimilarApps);
        when(appDetailService.getDeveloperApps(testAppId, 5)).thenReturn(new ArrayList<>());
        when(appDetailService.getCategoryTopApps(testAppId, 5)).thenReturn(testSimilarApps);

        // When & Then
        mockMvc.perform(get("/api/appstore/app/{appId}/related", testAppId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.appId").value(testAppId))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.similar").isArray())
                .andExpect(jsonPath("$.data.developer").isArray())
                .andExpect(jsonPath("$.data.categoryTop").isArray());

        // Verify
        verify(appDetailService, times(1)).getSimilarApps(testAppId, 5);
        verify(appDetailService, times(1)).getDeveloperApps(testAppId, 5);
        verify(appDetailService, times(1)).getCategoryTopApps(testAppId, 5);
    }

    @Test
    @DisplayName("记录下载 - 成功")
    void testRecordDownload_Success() throws Exception {
        // Given
        when(appDetailService.recordDownload(testAppId)).thenReturn(true);
        when(appDetailService.getAppStoreUrl(testAppId))
                .thenReturn("https://apps.apple.com/cn/app/id" + testAppId);

        // When & Then
        mockMvc.perform(post("/api/appstore/app/{appId}/download", testAppId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.appId").value(testAppId))
                .andExpect(jsonPath("$.appStoreUrl").value("https://apps.apple.com/cn/app/id" + testAppId))
                .andExpect(jsonPath("$.message").value("下载记录成功"));

        // Verify
        verify(appDetailService, times(1)).recordDownload(testAppId);
        verify(appDetailService, times(1)).getAppStoreUrl(testAppId);
    }

    @Test
    @DisplayName("记录下载 - 失败")
    void testRecordDownload_Failure() throws Exception {
        // Given
        when(appDetailService.recordDownload(testAppId)).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/appstore/app/{appId}/download", testAppId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.appId").value(testAppId))
                .andExpect(jsonPath("$.message").value("记录失败"));

        // Verify
        verify(appDetailService, times(1)).recordDownload(testAppId);
        verify(appDetailService, never()).getAppStoreUrl(anyString());
    }

    @Test
    @DisplayName("获取App Store链接 - 成功")
    void testGetAppStoreUrl_Success() throws Exception {
        // Given
        String expectedUrl = "https://apps.apple.com/cn/app/id" + testAppId;
        when(appDetailService.getAppStoreUrl(testAppId)).thenReturn(expectedUrl);

        // When & Then
        mockMvc.perform(get("/api/appstore/app/{appId}/store-url", testAppId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.url").value(expectedUrl))
                .andExpect(jsonPath("$.appId").value(testAppId));

        // Verify
        verify(appDetailService, times(1)).getAppStoreUrl(testAppId);
    }

    @Test
    @DisplayName("获取App Store链接 - 应用不存在")
    void testGetAppStoreUrl_NotFound() throws Exception {
        // Given
        when(appDetailService.getAppStoreUrl(testAppId)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/appstore/app/{appId}/store-url", testAppId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("应用不存在"))
                .andExpect(jsonPath("$.appId").value(testAppId));

        // Verify
        verify(appDetailService, times(1)).getAppStoreUrl(testAppId);
    }
}
package com.moon.cloud.appstore.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.appstore.dto.FreeAppQueryDTO;
import com.moon.cloud.appstore.service.FreePromotionService;
import com.moon.cloud.appstore.vo.FreeAppStatisticsVO;
import com.moon.cloud.appstore.vo.FreePromotionVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 限免推广控制器测试类
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("限免推广控制器测试")
class FreePromotionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FreePromotionService freePromotionService;

    @InjectMocks
    private FreePromotionController freePromotionController;

    private FreePromotionVO testPromotionVO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(freePromotionController).build();
        testPromotionVO = createTestPromotionVO();
    }

    @Test
    @DisplayName("测试获取今日限免列表接口")
    void testGetTodayFreeApps() throws Exception {
        // 准备数据
        Page<FreePromotionVO> mockPage = new Page<>(1, 20);
        mockPage.setRecords(Arrays.asList(testPromotionVO));
        mockPage.setTotal(1);

        // Mock行为
        when(freePromotionService.getTodayFreeApps(any(FreeAppQueryDTO.class))).thenReturn(mockPage);

        // 执行测试
        mockMvc.perform(get("/api/appstore/free/today")
                        .param("page", "1")
                        .param("size", "20")
                        .param("promotionType", "FREE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].appName", is("测试应用")));

        // 验证调用
        verify(freePromotionService, times(1)).getTodayFreeApps(any(FreeAppQueryDTO.class));
    }

    @Test
    @DisplayName("测试获取即将结束限免接口")
    void testGetEndingSoonApps() throws Exception {
        // 准备数据
        List<FreePromotionVO> mockList = Arrays.asList(testPromotionVO);

        // Mock行为
        when(freePromotionService.getEndingSoonApps(anyInt())).thenReturn(mockList);

        // 执行测试
        mockMvc.perform(get("/api/appstore/free/ending-soon")
                        .param("hours", "6")
                        .param("limit", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.hours", is(6)))
                .andExpect(jsonPath("$.count", is(1)))
                .andExpect(jsonPath("$.data", hasSize(1)));

        // 验证调用
        verify(freePromotionService, times(1)).getEndingSoonApps(6);
    }

    @Test
    @DisplayName("测试获取热门限免接口")
    void testGetHotFreeApps() throws Exception {
        // 准备数据
        testPromotionVO.setIsHot(true);
        testPromotionVO.setHotScore(1000);
        List<FreePromotionVO> mockList = Arrays.asList(testPromotionVO);

        // Mock行为
        when(freePromotionService.getHotFreeApps(anyInt())).thenReturn(mockList);

        // 执行测试
        mockMvc.perform(get("/api/appstore/free/hot")
                        .param("limit", "10")
                        .param("type", "FREE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.count", is(1)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].isHot", is(true)));

        // 验证调用
        verify(freePromotionService, times(1)).getHotFreeApps(10);
    }

    @Test
    @DisplayName("测试获取限免统计数据接口")
    void testGetFreeAppStatistics() throws Exception {
        // 准备数据
        FreeAppStatisticsVO statistics = new FreeAppStatisticsVO();
        statistics.setTodayFreeCount(10);
        statistics.setTodayDiscountCount(5);
        statistics.setActiveFreeCount(15);
        statistics.setTodayTotalSavings(new BigDecimal("200.00"));

        // Mock行为
        when(freePromotionService.getFreeAppStatistics()).thenReturn(statistics);

        // 执行测试
        mockMvc.perform(get("/api/appstore/free/statistics")
                        .param("period", "today")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.period", is("today")))
                .andExpect(jsonPath("$.data.todayFreeCount", is(10)))
                .andExpect(jsonPath("$.data.todayDiscountCount", is(5)));

        // 验证调用
        verify(freePromotionService, times(1)).getFreeAppStatistics();
    }

    @Test
    @DisplayName("测试按分类获取限免接口")
    void testGetFreeAppsByCategory() throws Exception {
        // 准备数据
        Page<FreePromotionVO> mockPage = new Page<>(1, 20);
        mockPage.setRecords(Arrays.asList(testPromotionVO));
        mockPage.setTotal(1);

        // Mock行为
        when(freePromotionService.getFreeAppsByCategory(anyString(), anyInt(), anyInt())).thenReturn(mockPage);

        // 执行测试
        mockMvc.perform(get("/api/appstore/free/category/6014")
                        .param("page", "1")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.categoryId", is("6014")))
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.data", hasSize(1)));

        // 验证调用
        verify(freePromotionService, times(1)).getFreeAppsByCategory("6014", 1, 20);
    }

    @Test
    @DisplayName("测试获取应用限免历史接口")
    void testGetPromotionHistory() throws Exception {
        // 准备数据
        List<FreePromotionVO> history = Arrays.asList(testPromotionVO, testPromotionVO);

        // Mock行为
        when(freePromotionService.getPromotionHistory(anyString())).thenReturn(history);

        // 执行测试
        mockMvc.perform(get("/api/appstore/free/app/123456/history")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.appId", is("123456")))
                .andExpect(jsonPath("$.count", is(2)))
                .andExpect(jsonPath("$.data", hasSize(2)));

        // 验证调用
        verify(freePromotionService, times(1)).getPromotionHistory("123456");
    }

    @Test
    @DisplayName("测试标记为已查看接口")
    void testMarkAsViewed() throws Exception {
        // Mock行为
        when(freePromotionService.markAsViewed(anyString())).thenReturn(true);

        // 执行测试
        mockMvc.perform(post("/api/appstore/free/1/view")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.promotionId", is("1")))
                .andExpect(jsonPath("$.message", is("标记成功")));

        // 验证调用
        verify(freePromotionService, times(1)).markAsViewed("1");
    }

    @Test
    @DisplayName("测试记录点击接口")
    void testRecordClick() throws Exception {
        // Mock行为
        when(freePromotionService.increaseClickCount(anyString())).thenReturn(true);

        // 执行测试
        mockMvc.perform(post("/api/appstore/free/1/click")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.promotionId", is("1")))
                .andExpect(jsonPath("$.message", is("记录成功")));

        // 验证调用
        verify(freePromotionService, times(1)).increaseClickCount("1");
    }

    @Test
    @DisplayName("测试更新限免状态接口")
    void testUpdatePromotionStatus() throws Exception {
        // Mock行为
        doNothing().when(freePromotionService).updatePromotionStatus();

        // 执行测试
        mockMvc.perform(post("/api/appstore/free/update-status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("状态更新任务已触发")));

        // 验证调用
        verify(freePromotionService, times(1)).updatePromotionStatus();
    }

    @Test
    @DisplayName("测试检测新限免接口")
    void testDetectNewPromotions() throws Exception {
        // Mock行为
        when(freePromotionService.detectNewPromotions()).thenReturn(5);

        // 执行测试
        mockMvc.perform(post("/api/appstore/free/detect-new")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.newCount", is(5)))
                .andExpect(jsonPath("$.message", is("发现 5 个新限免应用")));

        // 验证调用
        verify(freePromotionService, times(1)).detectNewPromotions();
    }

    @Test
    @DisplayName("测试分页参数验证")
    void testPaginationValidation() throws Exception {
        // 准备数据
        Page<FreePromotionVO> mockPage = new Page<>(1, 20);
        mockPage.setRecords(Arrays.asList(testPromotionVO));

        // Mock行为
        when(freePromotionService.getTodayFreeApps(any(FreeAppQueryDTO.class))).thenReturn(mockPage);

        // 测试无效的分页参数
        mockMvc.perform(get("/api/appstore/free/today")
                        .param("page", "-1")  // 无效页码
                        .param("size", "200")  // 超过最大限制
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // 验证参数被正确处理
        verify(freePromotionService, times(1)).getTodayFreeApps(any(FreeAppQueryDTO.class));
    }

    // ========== 辅助方法 ==========

    private FreePromotionVO createTestPromotionVO() {
        FreePromotionVO vo = new FreePromotionVO();
        vo.setId("1");
        vo.setAppId("app1");
        vo.setAppstoreAppId("123456");
        vo.setAppName("测试应用");
        vo.setIconUrl("https://example.com/icon.png");
        vo.setBundleId("com.test.app");
        vo.setDeveloperName("测试开发者");
        vo.setCategoryName("游戏");
        vo.setCategoryId("6014");
        vo.setPromotionType("FREE");
        vo.setOriginalPrice(new BigDecimal("18.00"));
        vo.setPromotionPrice(BigDecimal.ZERO);
        vo.setDiscountRate(new BigDecimal("100"));
        vo.setSavingsAmount(new BigDecimal("18.00"));
        vo.setRating(new BigDecimal("4.5"));
        vo.setRatingCount(1000);
        vo.setFileSize(100000000L);
        vo.setVersion("1.0.0");
        vo.setStartTime(LocalDateTime.now().minusHours(2));
        vo.setDiscoveredAt(LocalDateTime.now().minusHours(2));
        vo.setStatus("ACTIVE");
        vo.setIsNew(true);
        vo.setIsEndingSoon(false);
        vo.setIsHot(false);
        vo.setIsFeatured(false);
        vo.setViewCount(100);
        vo.setClickCount(50);
        vo.setShareCount(10);
        vo.setHotScore(500);
        vo.setTags(Arrays.asList("限免", "新发现"));
        return vo;
    }
}
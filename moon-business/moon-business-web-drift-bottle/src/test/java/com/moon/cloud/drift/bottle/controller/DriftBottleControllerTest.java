package com.moon.cloud.drift.bottle.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moon.cloud.drift.bottle.dto.BottleReplyDTO;
import com.moon.cloud.drift.bottle.dto.DriftBottleDTO;
import com.moon.cloud.drift.bottle.service.DriftBottleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 漂流瓶控制器测试类
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@WebMvcTest(DriftBottleController.class)
class DriftBottleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DriftBottleService driftBottleService;

    @Autowired
    private ObjectMapper objectMapper;

    private DriftBottleDTO testBottleDTO;
    private BottleReplyDTO testReplyDTO;

    @BeforeEach
    void setUp() {
        testBottleDTO = new DriftBottleDTO();
        testBottleDTO.setId(1L);
        testBottleDTO.setSenderUsername("testUser");
        testBottleDTO.setContent("这是一个测试漂流瓶");
        testBottleDTO.setStatus("FLOATING");
        testBottleDTO.setPassCount(0);
        testBottleDTO.setCreateTime(LocalDateTime.now());
        testBottleDTO.setLastUpdateTime(LocalDateTime.now());

        testReplyDTO = new BottleReplyDTO();
        testReplyDTO.setId(1L);
        testReplyDTO.setReplierUsername("replier");
        testReplyDTO.setReplyContent("这是一个回复");
        testReplyDTO.setReplyTime(LocalDateTime.now());
        testReplyDTO.setBottleId(1L);
    }

    @Test
    void testThrowBottle() throws Exception {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("senderUsername", "testUser");
        request.put("content", "这是一个测试漂流瓶");
        
        when(driftBottleService.createAndThrowBottle(any(DriftBottleDTO.class)))
                .thenReturn(testBottleDTO);

        // When & Then
        mockMvc.perform(post("/api/drift-bottle/throw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.senderUsername").value("testUser"))
                .andExpect(jsonPath("$.data.content").value("这是一个测试漂流瓶"));

        verify(driftBottleService, times(1)).createAndThrowBottle(any(DriftBottleDTO.class));
    }

    @Test
    void testThrowBottleWithInvalidContent() throws Exception {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("senderUsername", "testUser");
        request.put("content", ""); // 空内容

        // When & Then
        mockMvc.perform(post("/api/drift-bottle/throw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(driftBottleService, never()).createAndThrowBottle(any(DriftBottleDTO.class));
    }

    @Test
    void testPickUpBottle() throws Exception {
        // Given
        String username = "picker";
        when(driftBottleService.pickUpBottle(username)).thenReturn(testBottleDTO);

        // When & Then
        mockMvc.perform(post("/api/drift-bottle/pickup")
                        .param("username", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.senderUsername").value("testUser"));

        verify(driftBottleService, times(1)).pickUpBottle(username);
    }

    @Test
    void testPickUpBottleWhenNoneAvailable() throws Exception {
        // Given
        String username = "picker";
        when(driftBottleService.pickUpBottle(username)).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/drift-bottle/pickup")
                        .param("username", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("暂时没有可捡起的漂流瓶"));

        verify(driftBottleService, times(1)).pickUpBottle(username);
    }

    @Test
    void testDiscardBottle() throws Exception {
        // Given
        Long bottleId = 1L;
        String username = "picker";
        when(driftBottleService.discardBottle(bottleId, username)).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/drift-bottle/discard/{bottleId}", bottleId)
                        .param("username", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("漂流瓶已丢弃"));

        verify(driftBottleService, times(1)).discardBottle(bottleId, username);
    }

    @Test
    void testDiscardBottleWhenNotAuthorized() throws Exception {
        // Given
        Long bottleId = 1L;
        String username = "wrongUser";
        when(driftBottleService.discardBottle(bottleId, username)).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/drift-bottle/discard/{bottleId}", bottleId)
                        .param("username", username))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("无权限操作此漂流瓶"));

        verify(driftBottleService, times(1)).discardBottle(bottleId, username);
    }

    @Test
    void testReplyToBottle() throws Exception {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("replierUsername", "replier");
        request.put("replyContent", "这是一个回复");
        request.put("bottleId", 1L);
        
        when(driftBottleService.replyToBottle(any(BottleReplyDTO.class)))
                .thenReturn(testReplyDTO);

        // When & Then
        mockMvc.perform(post("/api/drift-bottle/reply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.replierUsername").value("replier"))
                .andExpect(jsonPath("$.data.replyContent").value("这是一个回复"));

        verify(driftBottleService, times(1)).replyToBottle(any(BottleReplyDTO.class));
    }

    @Test
    void testGetSentBottles() throws Exception {
        // Given
        String username = "testUser";
        Page<DriftBottleDTO> bottlePage = new PageImpl<>(Arrays.asList(testBottleDTO));
        
        when(driftBottleService.getSentBottles(username, 0, 10)).thenReturn(bottlePage);

        // When & Then
        mockMvc.perform(get("/api/drift-bottle/sent")
                        .param("username", username)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].senderUsername").value("testUser"));

        verify(driftBottleService, times(1)).getSentBottles(username, 0, 10);
    }

    @Test
    void testGetReceivedBottles() throws Exception {
        // Given
        String username = "testUser";
        Page<DriftBottleDTO> bottlePage = new PageImpl<>(Arrays.asList(testBottleDTO));
        
        when(driftBottleService.getReceivedBottles(username, 0, 10)).thenReturn(bottlePage);

        // When & Then
        mockMvc.perform(get("/api/drift-bottle/received")
                        .param("username", username)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].senderUsername").value("testUser"));

        verify(driftBottleService, times(1)).getReceivedBottles(username, 0, 10);
    }

    @Test
    void testGetBottleDetail() throws Exception {
        // Given
        Long bottleId = 1L;
        String username = "testUser";
        
        when(driftBottleService.getBottleDetail(bottleId, username)).thenReturn(testBottleDTO);

        // When & Then
        mockMvc.perform(get("/api/drift-bottle/detail/{bottleId}", bottleId)
                        .param("username", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.senderUsername").value("testUser"));

        verify(driftBottleService, times(1)).getBottleDetail(bottleId, username);
    }

    @Test
    void testGetBottleReplies() throws Exception {
        // Given
        Long bottleId = 1L;
        String username = "testUser";
        Page<BottleReplyDTO> replyPage = new PageImpl<>(Arrays.asList(testReplyDTO));
        
        when(driftBottleService.getBottleReplies(bottleId, username, 0, 10)).thenReturn(replyPage);

        // When & Then
        mockMvc.perform(get("/api/drift-bottle/{bottleId}/replies", bottleId)
                        .param("username", username)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].replierUsername").value("replier"));

        verify(driftBottleService, times(1)).getBottleReplies(bottleId, username, 0, 10);
    }

    @Test
    void testGetUserStatistics() throws Exception {
        // Given
        String username = "testUser";
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("sentCount", 5L);
        statistics.put("receivedCount", 3L);
        statistics.put("replyCount", 2L);
        
        when(driftBottleService.getUserStatistics(username)).thenReturn(statistics);

        // When & Then
        mockMvc.perform(get("/api/drift-bottle/statistics")
                        .param("username", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sentCount").value(5))
                .andExpect(jsonPath("$.data.receivedCount").value(3))
                .andExpect(jsonPath("$.data.replyCount").value(2));

        verify(driftBottleService, times(1)).getUserStatistics(username);
    }

    @Test
    void testCircuitBreakerFallback() throws Exception {
        // Given
        String username = "testUser";
        when(driftBottleService.pickUpBottle(username))
                .thenThrow(new RuntimeException("Service unavailable"));

        // When & Then
        // 注意：这个测试可能需要配置熔断器的阈值和时间窗口
        // 在实际测试中，可能需要多次调用来触发熔断器
        mockMvc.perform(post("/api/drift-bottle/pickup")
                        .param("username", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }
}
package com.moon.cloud.drift.bottle.service;

import com.moon.cloud.drift.bottle.dto.BottleReplyDTO;
import com.moon.cloud.drift.bottle.dto.DriftBottleDTO;
import com.moon.cloud.drift.bottle.entity.DriftBottle;
import com.moon.cloud.drift.bottle.repository.BottleReplyRepository;
import com.moon.cloud.drift.bottle.repository.DriftBottleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 漂流瓶服务测试类
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class DriftBottleServiceTest {

    @Mock
    private DriftBottleRepository driftBottleRepository;

    @Mock
    private BottleReplyRepository bottleReplyRepository;

    @InjectMocks
    private DriftBottleService driftBottleService;

    private DriftBottleDTO testBottleDTO;
    private DriftBottle testBottle;

    @BeforeEach
    void setUp() {
        testBottleDTO = new DriftBottleDTO("testUser", "这是一个测试漂流瓶");
        
        testBottle = new DriftBottle("testUser", "这是一个测试漂流瓶");
        testBottle.setId(1L);
        testBottle.setStatus(DriftBottle.BottleStatus.FLOATING);
        testBottle.setPassCount(0);
        testBottle.setCreateTime(LocalDateTime.now());
        testBottle.setLastUpdateTime(LocalDateTime.now());
    }

    @Test
    void testCreateAndThrowBottle() {
        // Given
        when(driftBottleRepository.save(any(DriftBottle.class))).thenReturn(testBottle);

        // When
        DriftBottleDTO result = driftBottleService.createAndThrowBottle(testBottleDTO);

        // Then
        assertNotNull(result);
        assertEquals("testUser", result.getSenderUsername());
        assertEquals("这是一个测试漂流瓶", result.getContent());
        assertEquals("FLOATING", result.getStatus());
        verify(driftBottleRepository, times(1)).save(any(DriftBottle.class));
    }

    @Test
    void testPickUpBottle() {
        // Given
        String username = "picker";
        List<DriftBottle> bottles = Arrays.asList(testBottle);
        when(driftBottleRepository.findRandomFloatingBottles(eq(username), any(Pageable.class)))
                .thenReturn(bottles);
        when(driftBottleRepository.save(any(DriftBottle.class))).thenReturn(testBottle);

        // When
        DriftBottleDTO result = driftBottleService.pickUpBottle(username);

        // Then
        assertNotNull(result);
        verify(driftBottleRepository, times(1)).findRandomFloatingBottles(eq(username), any(Pageable.class));
        verify(driftBottleRepository, times(1)).save(any(DriftBottle.class));
    }

    @Test
    void testPickUpBottleWhenNoneAvailable() {
        // Given
        String username = "picker";
        when(driftBottleRepository.findRandomFloatingBottles(eq(username), any(Pageable.class)))
                .thenReturn(Arrays.asList());

        // When
        DriftBottleDTO result = driftBottleService.pickUpBottle(username);

        // Then
        assertNull(result);
        verify(driftBottleRepository, times(1)).findRandomFloatingBottles(eq(username), any(Pageable.class));
        verify(driftBottleRepository, never()).save(any(DriftBottle.class));
    }

    @Test
    void testDiscardBottle() {
        // Given
        Long bottleId = 1L;
        String username = "picker";
        testBottle.setCurrentHolder(username);
        testBottle.setStatus(DriftBottle.BottleStatus.PICKED_UP);
        testBottle.setPassCount(5);
        
        when(driftBottleRepository.findById(bottleId)).thenReturn(Optional.of(testBottle));
        when(driftBottleRepository.save(any(DriftBottle.class))).thenReturn(testBottle);

        // When
        boolean result = driftBottleService.discardBottle(bottleId, username);

        // Then
        assertTrue(result);
        verify(driftBottleRepository, times(1)).findById(bottleId);
        verify(driftBottleRepository, times(1)).save(any(DriftBottle.class));
    }

    @Test
    void testDiscardBottleWhenNotHolder() {
        // Given
        Long bottleId = 1L;
        String username = "wrongUser";
        testBottle.setCurrentHolder("rightUser");
        
        when(driftBottleRepository.findById(bottleId)).thenReturn(Optional.of(testBottle));

        // When
        boolean result = driftBottleService.discardBottle(bottleId, username);

        // Then
        assertFalse(result);
        verify(driftBottleRepository, times(1)).findById(bottleId);
        verify(driftBottleRepository, never()).save(any(DriftBottle.class));
    }

    @Test
    void testReplyToBottle() {
        // Given
        BottleReplyDTO replyDTO = new BottleReplyDTO("replier", "这是一个回复", 1L);
        testBottle.setCurrentHolder("replier");
        testBottle.setStatus(DriftBottle.BottleStatus.PICKED_UP);
        
        when(driftBottleRepository.findById(1L)).thenReturn(Optional.of(testBottle));
        when(bottleReplyRepository.save(any())).thenReturn(null);
        when(driftBottleRepository.save(any(DriftBottle.class))).thenReturn(testBottle);

        // When
        BottleReplyDTO result = driftBottleService.replyToBottle(replyDTO);

        // Then
        assertNotNull(result);
        verify(driftBottleRepository, times(1)).findById(1L);
        verify(bottleReplyRepository, times(1)).save(any());
        verify(driftBottleRepository, times(1)).save(any(DriftBottle.class));
    }

    @Test
    void testReplyToBottleWhenNotHolder() {
        // Given
        BottleReplyDTO replyDTO = new BottleReplyDTO("wrongUser", "这是一个回复", 1L);
        testBottle.setCurrentHolder("rightUser");
        
        when(driftBottleRepository.findById(1L)).thenReturn(Optional.of(testBottle));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            driftBottleService.replyToBottle(replyDTO);
        });
        
        verify(driftBottleRepository, times(1)).findById(1L);
        verify(bottleReplyRepository, never()).save(any());
    }

    @Test
    void testGetSentBottles() {
        // Given
        String username = "testUser";
        List<DriftBottle> bottles = Arrays.asList(testBottle);
        Page<DriftBottle> bottlePage = new PageImpl<>(bottles);
        
        when(driftBottleRepository.findBySenderUsername(eq(username), any(Pageable.class)))
                .thenReturn(bottlePage);

        // When
        Page<DriftBottleDTO> result = driftBottleService.getSentBottles(username, 0, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(driftBottleRepository, times(1)).findBySenderUsername(eq(username), any(Pageable.class));
    }

    @Test
    void testGetReceivedBottles() {
        // Given
        String username = "testUser";
        List<DriftBottle> bottles = Arrays.asList(testBottle);
        Page<DriftBottle> bottlePage = new PageImpl<>(bottles);
        
        when(driftBottleRepository.findByCurrentHolder(eq(username), any(Pageable.class)))
                .thenReturn(bottlePage);

        // When
        Page<DriftBottleDTO> result = driftBottleService.getReceivedBottles(username, 0, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(driftBottleRepository, times(1)).findByCurrentHolder(eq(username), any(Pageable.class));
    }

    @Test
    void testGetBottleDetail() {
        // Given
        Long bottleId = 1L;
        String username = "testUser";
        testBottle.setSenderUsername(username);
        
        when(driftBottleRepository.findById(bottleId)).thenReturn(Optional.of(testBottle));

        // When
        DriftBottleDTO result = driftBottleService.getBottleDetail(bottleId, username);

        // Then
        assertNotNull(result);
        assertEquals(username, result.getSenderUsername());
        verify(driftBottleRepository, times(1)).findById(bottleId);
    }

    @Test
    void testGetBottleDetailWhenNoPermission() {
        // Given
        Long bottleId = 1L;
        String username = "wrongUser";
        testBottle.setSenderUsername("rightUser");
        testBottle.setCurrentHolder("anotherUser");
        
        when(driftBottleRepository.findById(bottleId)).thenReturn(Optional.of(testBottle));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            driftBottleService.getBottleDetail(bottleId, username);
        });
        
        verify(driftBottleRepository, times(1)).findById(bottleId);
    }

    @Test
    void testGetUserStatistics() {
        // Given
        String username = "testUser";
        when(driftBottleRepository.countBySenderUsername(username)).thenReturn(5L);
        when(driftBottleRepository.countByCurrentHolder(username)).thenReturn(3L);
        when(bottleReplyRepository.countByReplierUsername(username)).thenReturn(2L);

        // When
        var result = driftBottleService.getUserStatistics(username);

        // Then
        assertNotNull(result);
        assertEquals(5L, result.get("sentCount"));
        assertEquals(3L, result.get("receivedCount"));
        assertEquals(2L, result.get("replyCount"));
        
        verify(driftBottleRepository, times(1)).countBySenderUsername(username);
        verify(driftBottleRepository, times(1)).countByCurrentHolder(username);
        verify(bottleReplyRepository, times(1)).countByReplierUsername(username);
    }
}
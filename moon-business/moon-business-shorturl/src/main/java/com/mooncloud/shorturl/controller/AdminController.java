package com.mooncloud.shorturl.controller;

import com.mooncloud.shorturl.entity.UrlMappingEntity;
import com.mooncloud.shorturl.entity.UrlAccessLogEntity;
import com.mooncloud.shorturl.enums.UrlStatus;
import com.mooncloud.shorturl.repository.UrlMappingRepository;
import com.mooncloud.shorturl.repository.UrlAccessLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 管理控制器
 * 
 * @author mooncloud
 */
@Controller
@RequestMapping("/admin")
@Slf4j
public class AdminController {
    
    @Autowired
    private UrlMappingRepository urlMappingRepository;
    
    @Autowired
    private UrlAccessLogRepository urlAccessLogRepository;
    
    /**
     * 管理首页
     */
    @GetMapping
    public String adminHome(Model model) {
        // 获取统计信息
        long totalUrls = urlMappingRepository.count();
        long activeUrls = urlMappingRepository.countByStatus(UrlStatus.ACTIVE);
        long totalClicks = urlAccessLogRepository.count();
        
        model.addAttribute("totalUrls", totalUrls);
        model.addAttribute("activeUrls", activeUrls);
        model.addAttribute("totalClicks", totalClicks);
        
        return "admin/dashboard";
    }
    
    /**
     * 短链列表页面
     */
    @GetMapping("/urls")
    public String urlList(@RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "20") int size,
                         @RequestParam(required = false) String keyword,
                         @RequestParam(required = false) UrlStatus status,
                         Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UrlMappingEntity> urlPage;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            urlPage = urlMappingRepository.findByOriginalUrlContainingOrShortUrlContaining(
                keyword, keyword, pageable);
        } else if (status != null) {
            urlPage = urlMappingRepository.findByStatus(status, pageable);
        } else {
            urlPage = urlMappingRepository.findAll(pageable);
        }
        
        model.addAttribute("urlPage", urlPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("statuses", UrlStatus.values());
        
        return "admin/url-list";
    }
    
    /**
     * 短链详情页面
     */
    @GetMapping("/urls/{shortUrl}")
    public String urlDetail(@PathVariable String shortUrl, Model model) {
        Optional<UrlMappingEntity> urlOpt = urlMappingRepository.findByShortUrl(shortUrl);
        if (urlOpt.isEmpty()) {
            return "redirect:/admin/urls?error=notfound";
        }
        
        UrlMappingEntity url = urlOpt.get();
        
        // 获取访问日志
        Pageable pageable = PageRequest.of(0, 50, Sort.by("accessTime").descending());
        Page<UrlAccessLogEntity> accessLogs = urlAccessLogRepository.findByShortUrlOrderByAccessTimeDesc(shortUrl, pageable);
        
        model.addAttribute("url", url);
        model.addAttribute("accessLogs", accessLogs);
        
        return "admin/url-detail";
    }
    
    /**
     * 更新短链状态
     */
    @PostMapping("/urls/{shortUrl}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateUrlStatus(@PathVariable String shortUrl,
                                                              @RequestParam UrlStatus status) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Optional<UrlMappingEntity> urlOpt = urlMappingRepository.findByShortUrl(shortUrl);
            if (urlOpt.isEmpty()) {
                result.put("success", false);
                result.put("message", "短链不存在");
                return ResponseEntity.badRequest().body(result);
            }
            
            UrlMappingEntity url = urlOpt.get();
            url.setStatus(status);
            urlMappingRepository.save(url);
            
            result.put("success", true);
            result.put("message", "状态更新成功");
            
            log.info("短链状态更新: {} -> {}", shortUrl, status);
            
        } catch (Exception e) {
            log.error("更新短链状态失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 删除短链
     */
    @DeleteMapping("/urls/{shortUrl}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteUrl(@PathVariable String shortUrl) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Optional<UrlMappingEntity> urlOpt = urlMappingRepository.findByShortUrl(shortUrl);
            if (urlOpt.isEmpty()) {
                result.put("success", false);
                result.put("message", "短链不存在");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 软删除：更新状态为已删除
            UrlMappingEntity url = urlOpt.get();
            url.setStatus(UrlStatus.DELETED);
            urlMappingRepository.save(url);
            
            result.put("success", true);
            result.put("message", "删除成功");
            
            log.info("短链已删除: {}", shortUrl);
            
        } catch (Exception e) {
            log.error("删除短链失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取统计数据API
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 基础统计
            stats.put("totalUrls", urlMappingRepository.count());
            stats.put("activeUrls", urlMappingRepository.countByStatus(UrlStatus.ACTIVE));
            stats.put("expiredUrls", urlMappingRepository.countByStatus(UrlStatus.EXPIRED));
            stats.put("disabledUrls", urlMappingRepository.countByStatus(UrlStatus.DISABLED));
            stats.put("totalClicks", urlAccessLogRepository.count());
            
            // 今日统计
            stats.put("todayClicks", urlAccessLogRepository.countTodayAccess());
            
            // 设备类型统计
            List<Object[]> deviceStats = urlAccessLogRepository.countByDeviceType();
            Map<String, Long> deviceMap = new HashMap<>();
            for (Object[] row : deviceStats) {
                deviceMap.put((String) row[0], (Long) row[1]);
            }
            stats.put("deviceStats", deviceMap);
            
            // 浏览器统计
            Long totalBrowserAccess = urlAccessLogRepository.countByBrowser();
            stats.put("totalBrowserAccess", totalBrowserAccess);
            
        } catch (Exception e) {
            log.error("获取统计数据失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 批量更新过期短链
     */
    @PostMapping("/api/update-expired")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateExpiredUrls() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int updatedCount = urlMappingRepository.updateExpiredUrls(new Date());
            result.put("success", true);
            result.put("message", "已更新 " + updatedCount + " 个过期短链");
            result.put("updatedCount", updatedCount);
            
            log.info("批量更新过期短链完成，更新数量: {}", updatedCount);
            
        } catch (Exception e) {
            log.error("批量更新过期短链失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 分区管理页面
     */
    @GetMapping("/partition")
    public String partitionPage() {
        return "partition";
    }
}
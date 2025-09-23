package com.mooncloud.shorturl.controller;

import com.mooncloud.shorturl.dto.ApiResponse;
import com.mooncloud.shorturl.dto.HotDataScore;
import com.mooncloud.shorturl.service.HotDataDetectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 热点数据分析控制器
 *
 * 提供热点数据识别和分析相关的API接口：
 * 1. 热点数据排行榜
 * 2. 单个数据热度分析
 * 3. 热点级别统计
 * 4. 新兴热点检测
 * 5. 热点趋势分析
 *
 * @author mooncloud
 */
@RestController
@RequestMapping("/api/v1/analytics/hotdata")
@Slf4j
@Validated
public class HotDataAnalysisController {

    @Autowired
    private HotDataDetectionService hotDataDetectionService;

    /**
     * 获取热点数据排行榜
     *
     * @param limit 返回数量限制
     * @return 热点数据排行榜
     */
    @GetMapping("/ranking")
    public ApiResponse<List<HotDataScore>> getHotRanking(
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer limit) {

        log.debug("获取热点数据排行榜，限制数量: {}", limit);

        List<HotDataScore> ranking = hotDataDetectionService.getHotRanking(limit);

        return ApiResponse.success("热点数据排行榜", ranking);
    }

    /**
     * 分析单个短链的热度
     *
     * @param shortCode 短链标识符
     * @return 热度分析结果
     */
    @GetMapping("/analyze/{shortCode}")
    public ApiResponse<HotDataScore> analyzeHotScore(@PathVariable String shortCode) {
        log.debug("分析短链热度: {}", shortCode);

        HotDataScore hotScore = hotDataDetectionService.calculateHotScore(shortCode);

        return ApiResponse.success("热度分析结果", hotScore);
    }

    /**
     * 批量分析多个短链的热度
     *
     * @param shortCodes 短链列表
     * @return 热度分析结果列表
     */
    @PostMapping("/analyze/batch")
    public ApiResponse<List<HotDataScore>> batchAnalyzeHotScore(@RequestBody List<String> shortCodes) {
        if (shortCodes == null || shortCodes.isEmpty()) {
            return ApiResponse.badRequest("短链列表不能为空");
        }

        if (shortCodes.size() > 50) {
            return ApiResponse.badRequest("批量分析最多支持50个短链");
        }

        log.debug("批量分析短链热度，数量: {}", shortCodes.size());

        List<HotDataScore> results = hotDataDetectionService.batchCalculateHotScore(shortCodes);

        return ApiResponse.success("批量热度分析结果", results);
    }

    /**
     * 获取指定热点级别的数据
     *
     * @param level 热点级别
     * @param limit 数量限制
     * @return 指定级别的热点数据
     */
    @GetMapping("/level/{level}")
    public ApiResponse<List<HotDataScore>> getHotDataByLevel(
            @PathVariable String level,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer limit) {

        try {
            HotDataScore.HotLevel hotLevel = HotDataScore.HotLevel.valueOf(level.toUpperCase());
            log.debug("获取热点级别数据: {}, 限制数量: {}", hotLevel, limit);

            List<HotDataScore> results = hotDataDetectionService.getHotDataByLevel(hotLevel, limit);

            return ApiResponse.success(String.format("%s级别热点数据", hotLevel.getDescription()), results);

        } catch (IllegalArgumentException e) {
            return ApiResponse.badRequest("无效的热点级别: " + level);
        }
    }

    /**
     * 获取新兴热点
     *
     * @return 新兴热点列表
     */
    @GetMapping("/emerging")
    public ApiResponse<List<HotDataScore>> getEmergingHotspots() {
        log.debug("检测新兴热点");

        List<HotDataScore> emergingHotspots = hotDataDetectionService.detectEmergingHotspots();

        return ApiResponse.success("新兴热点数据", emergingHotspots);
    }

    /**
     * 获取热点级别统计
     *
     * @return 各级别热点数量统计
     */
    @GetMapping("/level/stats")
    public ApiResponse<Map<String, Object>> getHotLevelStats() {
        log.debug("获取热点级别统计");

        // 获取大量数据进行统计
        List<HotDataScore> allHotData = hotDataDetectionService.getHotRanking(500);

        // 按级别分组统计
        Map<HotDataScore.HotLevel, Long> levelCounts = allHotData.stream()
                .collect(Collectors.groupingBy(
                        HotDataScore::getHotLevel,
                        Collectors.counting()
                ));

        // 转换为前端友好的格式
        Map<String, Object> stats = levelCounts.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().name().toLowerCase(),
                        entry -> Map.of(
                                "count", entry.getValue(),
                                "description", entry.getKey().getDescription(),
                                "percentage", Math.round(entry.getValue() * 100.0 / allHotData.size() * 100.0) / 100.0
                        )
                ));

        return ApiResponse.success("热点级别统计", stats);
    }

    /**
     * 获取热点分数分布
     *
     * @return 分数区间分布统计
     */
    @GetMapping("/score/distribution")
    public ApiResponse<Map<String, Object>> getScoreDistribution() {
        log.debug("获取热点分数分布");

        List<HotDataScore> allHotData = hotDataDetectionService.getHotRanking(500);

        // 分数区间统计
        Map<String, Long> distribution = allHotData.stream()
                .collect(Collectors.groupingBy(
                        score -> {
                            double s = score.getTotalScore();
                            if (s >= 90) return "90-100";
                            if (s >= 80) return "80-89";
                            if (s >= 70) return "70-79";
                            if (s >= 60) return "60-69";
                            if (s >= 50) return "50-59";
                            if (s >= 40) return "40-49";
                            if (s >= 30) return "30-39";
                            if (s >= 20) return "20-29";
                            if (s >= 10) return "10-19";
                            return "0-9";
                        },
                        Collectors.counting()
                ));

        // 计算平均分数
        double avgScore = allHotData.stream()
                .mapToDouble(HotDataScore::getTotalScore)
                .average()
                .orElse(0.0);

        Map<String, Object> result = Map.of(
                "distribution", distribution,
                "totalCount", allHotData.size(),
                "averageScore", Math.round(avgScore * 100.0) / 100.0
        );

        return ApiResponse.success("热点分数分布", result);
    }

    /**
     * 获取热点趋势分析
     *
     * @param shortCode 短链标识符
     * @return 趋势分析结果
     */
    @GetMapping("/trend/{shortCode}")
    public ApiResponse<Map<String, Object>> getTrendAnalysis(@PathVariable String shortCode) {
        log.debug("获取热点趋势分析: {}", shortCode);

        HotDataScore hotScore = hotDataDetectionService.calculateHotScore(shortCode);

        // 构建趋势分析结果
        Map<String, Object> trendAnalysis = Map.of(
                "shortCode", shortCode,
                "currentScore", hotScore.getTotalScore(),
                "hotLevel", hotScore.getHotLevel(),
                "trend", Map.of(
                        "score", hotScore.getTrendScore(),
                        "description", getTrendDescription(hotScore.getTrendScore())
                ),
                "metrics", Map.of(
                        "frequency", hotScore.getAccessFrequencyScore(),
                        "timeliness", hotScore.getTimelinessScore(),
                        "userDistribution", hotScore.getUserDistributionScore(),
                        "geographic", hotScore.getGeographicScore()
                ),
                "recommendations", generateRecommendations(hotScore)
        );

        return ApiResponse.success("趋势分析结果", trendAnalysis);
    }

    /**
     * 清理热度分数缓存
     *
     * @return 清理结果
     */
    @PostMapping("/cache/clear")
    public ApiResponse<String> clearHotScoreCache() {
        log.info("清理热度分数缓存");

        hotDataDetectionService.clearHotScoreCache();

        return ApiResponse.success("热度分数缓存已清理");
    }

    /**
     * 获取热点级别枚举信息
     *
     * @return 热点级别信息
     */
    @GetMapping("/levels")
    public ApiResponse<List<Map<String, Object>>> getHotLevels() {
        List<Map<String, Object>> levels = List.of(
                Map.of("level", "SUPER_HOT", "description", "超级热点", "minScore", 90, "maxScore", 100),
                Map.of("level", "HOT", "description", "热点", "minScore", 70, "maxScore", 89),
                Map.of("level", "WARM", "description", "温热", "minScore", 50, "maxScore", 69),
                Map.of("level", "NORMAL", "description", "普通", "minScore", 30, "maxScore", 49),
                Map.of("level", "COLD", "description", "冷门", "minScore", 0, "maxScore", 29)
        );

        return ApiResponse.success("热点级别信息", levels);
    }

    /**
     * 获取趋势描述
     */
    private String getTrendDescription(Double trendScore) {
        if (trendScore >= 80) return "强烈上升趋势";
        if (trendScore >= 60) return "上升趋势";
        if (trendScore >= 40) return "平稳趋势";
        if (trendScore >= 20) return "下降趋势";
        return "强烈下降趋势";
    }

    /**
     * 生成优化建议
     */
    private List<String> generateRecommendations(HotDataScore hotScore) {
        List<String> recommendations = new java.util.ArrayList<>();

        if (hotScore.getAccessFrequencyScore() < 30) {
            recommendations.add("访问频次较低，建议加强推广");
        }

        if (hotScore.getTimelinessScore() < 30) {
            recommendations.add("时效性较低，建议重新激活该链接");
        }

        if (hotScore.getTrendScore() < 30) {
            recommendations.add("访问趋势下降，建议分析原因并采取措施");
        }

        if (hotScore.getUserDistributionScore() < 30) {
            recommendations.add("用户分布单一，建议扩大推广渠道");
        }

        if (hotScore.getGeographicScore() < 30) {
            recommendations.add("地域分布有限，建议拓展不同地区用户");
        }

        if (hotScore.getTotalScore() >= 80) {
            recommendations.add("热度很高，建议优先预热缓存");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("数据表现良好，保持当前策略");
        }

        return recommendations;
    }
}
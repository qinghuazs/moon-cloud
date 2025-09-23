package com.mooncloud.shorturl.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 热点数据评分模型
 *
 * 通过多维度指标计算短链的热度分数
 *
 * @author mooncloud
 */
@Data
@Builder
public class HotDataScore {

    /**
     * 短链标识符
     */
    private String shortCode;

    /**
     * 综合热度分数 (0-100)
     */
    private Double totalScore;

    /**
     * 访问频次分数 (0-100)
     */
    private Double accessFrequencyScore;

    /**
     * 时效性分数 (0-100)
     */
    private Double timelinessScore;

    /**
     * 趋势分数 (0-100)
     */
    private Double trendScore;

    /**
     * 用户分布分数 (0-100)
     */
    private Double userDistributionScore;

    /**
     * 地域分布分数 (0-100)
     */
    private Double geographicScore;

    /**
     * 热点级别
     */
    private HotLevel hotLevel;

    /**
     * 总点击次数
     */
    private Long totalClicks;

    /**
     * 近期点击次数 (最近24小时)
     */
    private Long recentClicks;

    /**
     * 独立用户数
     */
    private Long uniqueUsers;

    /**
     * 独立IP数
     */
    private Long uniqueIps;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessTime;

    /**
     * 计算时间
     */
    private LocalDateTime calculatedAt;

    /**
     * 热点级别枚举
     */
    public enum HotLevel {
        /**
         * 超级热点 (90-100分)
         */
        SUPER_HOT("超级热点", 90, 100),

        /**
         * 热点 (70-89分)
         */
        HOT("热点", 70, 89),

        /**
         * 温热 (50-69分)
         */
        WARM("温热", 50, 69),

        /**
         * 普通 (30-49分)
         */
        NORMAL("普通", 30, 49),

        /**
         * 冷门 (0-29分)
         */
        COLD("冷门", 0, 29);

        private final String description;
        private final int minScore;
        private final int maxScore;

        HotLevel(String description, int minScore, int maxScore) {
            this.description = description;
            this.minScore = minScore;
            this.maxScore = maxScore;
        }

        public String getDescription() {
            return description;
        }

        public int getMinScore() {
            return minScore;
        }

        public int getMaxScore() {
            return maxScore;
        }

        /**
         * 根据分数获取热点级别
         */
        public static HotLevel fromScore(double score) {
            for (HotLevel level : values()) {
                if (score >= level.minScore && score <= level.maxScore) {
                    return level;
                }
            }
            return COLD;
        }
    }
}
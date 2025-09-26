package com.moon.cloud.captcha.config;

import com.moon.cloud.captcha.enums.CaptchaType;
import com.moon.cloud.captcha.enums.StorageType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 验证码配置属性
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "moon.captcha")
public class CaptchaProperties {

    /**
     * 是否启用验证码功能
     */
    private boolean enabled = true;

    /**
     * 验证码类型
     */
    private CaptchaType type = CaptchaType.MIXED;

    /**
     * 验证码长度
     */
    private int length = 6;

    /**
     * 验证码有效期（秒）
     */
    private long expireTime = 300;

    /**
     * 存储类型
     */
    private StorageType storage = StorageType.MEMORY;

    /**
     * Redis配置
     */
    private Redis redis = new Redis();

    /**
     * 安全配置
     */
    private Security security = new Security();

    /**
     * 图形验证码配置
     */
    private Image image = new Image();

    /**
     * 算术验证码配置
     */
    private Math math = new Math();

    @Data
    public static class Redis {
        /**
         * 键前缀
         */
        private String keyPrefix = "captcha:";

        /**
         * 失败记录键前缀
         */
        private String failurePrefix = "captcha:fail:";
    }

    @Data
    public static class Security {
        /**
         * 最大重试次数
         */
        private int maxRetry = 5;

        /**
         * 锁定时间（秒）
         */
        private long lockTime = 1800;

        /**
         * 是否大小写敏感
         */
        private boolean caseSensitive = false;

        /**
         * 是否允许重复使用
         */
        private boolean allowReuse = false;
    }

    @Data
    public static class Image {
        /**
         * 图片宽度
         */
        private int width = 120;

        /**
         * 图片高度
         */
        private int height = 40;

        /**
         * 字体大小
         */
        private int fontSize = 24;

        /**
         * 字体名称
         */
        private String fontName = "Arial";

        /**
         * 噪点数量
         */
        private int noiseCount = 10;

        /**
         * 干扰线数量
         */
        private int lineCount = 5;

        /**
         * 边框
         */
        private boolean border = true;

        /**
         * 边框颜色
         */
        private String borderColor = "#000000";

        /**
         * 背景色
         */
        private String backgroundColor = "#FFFFFF";

        /**
         * 字体颜色
         */
        private String fontColor = "#000000";
    }

    @Data
    public static class Math {
        /**
         * 运算符类型：+,-,*,/
         */
        private String operators = "+-";

        /**
         * 最小数字
         */
        private int minNumber = 1;

        /**
         * 最大数字
         */
        private int maxNumber = 10;

        /**
         * 是否显示等式
         */
        private boolean showEquation = true;
    }
}
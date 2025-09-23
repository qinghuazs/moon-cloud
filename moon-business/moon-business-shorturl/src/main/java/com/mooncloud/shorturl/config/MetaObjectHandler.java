package com.mooncloud.shorturl.config;

import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * MyBatis Plus字段自动填充处理器
 *
 * @author mooncloud
 */
@Component
public class MetaObjectHandler implements com.baomidou.mybatisplus.core.handlers.MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        Date now = new Date();
        this.strictInsertFill(metaObject, "createdAt", Date.class, now);
        this.strictInsertFill(metaObject, "updatedAt", Date.class, now);
        this.strictInsertFill(metaObject, "accessTime", Date.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", Date.class, new Date());
    }
}
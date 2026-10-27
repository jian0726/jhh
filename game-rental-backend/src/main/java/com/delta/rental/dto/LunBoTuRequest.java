package com.delta.rental.dto;

import lombok.Data;

/**
 * 轮播图新增/修改请求。
 *
 * <p>典型流程：前端先调 {@code POST /api/upload/image} 上传图片拿到绝对 URL，
 * 再把该 URL 填进 {@link #tuPianUrl} 调用 {@code POST /api/lunbotu} 保存。
 * 数据库只存 URL，图片文件由后端托管。
 */
@Data
public class LunBoTuRequest {

    /** 图片访问 URL（由 /api/upload/image 上传后返回） */
    private String tuPianUrl;

    /** 兼容字段名：部分页面可能用 imageUrl */
    private String imageUrl;

    /** 轮播标题（实体字段名为 tieTle，历史拼写保持不变） */
    private String tieTle;

    /** 兼容字段名：title */
    private String title;

    /** 点击跳转链接 */
    private String lianJieUrl;

    /** 排序号，越小越靠前 */
    private Integer shunXu;

    /** 状态：1=启用（首页可见） 0=停用 */
    private Integer zhuangTai;
}

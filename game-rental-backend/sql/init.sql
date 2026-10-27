-- ============================================================
-- 三角洲行动游戏账号租赁平台 —— 数据库初始化脚本
-- ============================================================
-- 命名规范：表名与字段名统一使用 snake_case（下划线小写）。
--   依据：实体类（com.delta.rental.entity）未显式声明 @Column 的字段，
--   由 Spring Boot 默认命名策略 CamelCaseToUnderscoresNamingStrategy
--   转为 snake_case（如 yongHuMing -> yong_hu_ming）。
--   本脚本的表结构与 Hibernate 生成的结果完全一致，
--   以保证 ddl-auto=update 启动时不再产生任何 ALTER TABLE。
--
-- 【重要】如果你之前用的是 camelCase 列名的旧版脚本，必须整体重建库：
--   DROP DATABASE sanJiaoZhouXingDong;
--   否则旧表会残留 camelCase 列，与实体映射不一致（表现为数据写入/读取不到）。
--
-- 执行方式：
--   mysql -uroot -p < sql/init.sql
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS sanJiaoZhouXingDong
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE sanJiaoZhouXingDong;

-- ------------------------------------------------------------
-- 用户表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `yonghu` (
    `id`                 bigint       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `yong_hu_ming`       varchar(50)  NOT NULL COMMENT '用户名',
    `mi_ma`              varchar(100) NOT NULL COMMENT '密码（BCrypt 哈希）',
    `shou_ji_hao`        varchar(20)  DEFAULT NULL COMMENT '手机号',
    `email`              varchar(100) DEFAULT NULL COMMENT '邮箱',
    `zhen_shi_xing_ming` varchar(50)  DEFAULT NULL COMMENT '真实姓名',
    `shen_fen_zheng`     varchar(18)  DEFAULT NULL COMMENT '身份证号',
    `shi_fou_shi_ming`   int          NOT NULL COMMENT '是否实名：0-未实名，1-已实名',
    `jue_se`             int          NOT NULL COMMENT '角色：0-普通用户，1-卖家，2-管理员',
    `zhuang_tai`         int          NOT NULL COMMENT '状态：0-正常，1-封禁',
    `open_id`            varchar(100) DEFAULT NULL COMMENT '第三方登录OpenID',
    `open_id_type`       varchar(20)  DEFAULT NULL COMMENT '第三方登录类型：wechat-微信，qq-QQ',
    `chuang_jian_shi_jian` datetime(6) NOT NULL COMMENT '创建时间',
    `geng_xin_shi_jian`  datetime(6)  DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_yong_hu_ming` (`yong_hu_ming`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_shou_ji_hao` (`shou_ji_hao`),
    KEY `idx_open_id` (`open_id`, `open_id_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ------------------------------------------------------------
-- 商品表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `shangpin` (
    `id`                     bigint        NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `mai_jia_id`             bigint        NOT NULL COMMENT '卖家用户ID',
    `biao_ti`                varchar(100)  NOT NULL COMMENT '商品标题',
    `miao_shu`               text COMMENT '商品描述',
    `zhang_hao_miao_shu`     text COMMENT '账号描述',
    `you_xi_zhang_hao`       varchar(100)  DEFAULT NULL COMMENT '游戏账号',
    `you_xi_mi_ma`           varchar(100)  DEFAULT NULL COMMENT '游戏密码',
    `ri_zu_jia_ge`           decimal(10,2) NOT NULL COMMENT '日租价格',
    `zui_shao_zu_jie_tian_shu` int         NOT NULL COMMENT '最少租借天数',
    `zhuang_tai`             int           NOT NULL COMMENT '状态：0-待审核，1-上架，2-已借出，3-下架',
    `zu_jie_ci_shu`          int           NOT NULL COMMENT '租借次数',
    `metadata`               json          DEFAULT NULL COMMENT '扩展元数据（JSON 字符串）',
    `fa_bu_shi_jian`         datetime(6)   NOT NULL COMMENT '发布时间',
    `chuang_jian_shi_jian`   datetime(6)   NOT NULL COMMENT '创建时间',
    `geng_xin_shi_jian`      datetime(6)   DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_mai_jia_id` (`mai_jia_id`),
    KEY `idx_zhuang_tai` (`zhuang_tai`),
    KEY `idx_ri_zu_jia_ge` (`ri_zu_jia_ge`),
    -- 中文全文检索必须使用 ngram 解析器：默认解析器按空格切词，中文整段会被当成一个词，搜不到
    FULLTEXT KEY `ft_shangpin` (`biao_ti`, `miao_shu`, `you_xi_zhang_hao`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- ------------------------------------------------------------
-- 订单表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `dingdan` (
    `id`                      bigint        NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `ding_dan_hao`            varchar(32)   NOT NULL COMMENT '订单号',
    `shang_pin_id`            bigint        NOT NULL COMMENT '商品ID',
    `mai_jia_id`              bigint        NOT NULL COMMENT '卖家用户ID',
    `mai_jia_id2`             bigint        NOT NULL COMMENT '买家用户ID',
    `zu_jie_tian_shu`         int           NOT NULL COMMENT '租借天数',
    `zong_jine`               decimal(10,2) NOT NULL COMMENT '总金额',
    `zhuang_tai`              int           NOT NULL COMMENT '状态：0-待支付，1-已支付，2-已完成，3-已取消，4-纠纷中',
    `zu_jie_kai_shi_shi_jian` datetime(6)   DEFAULT NULL COMMENT '租借开始时间',
    `zu_jie_jie_shu_shi_jian` datetime(6)   DEFAULT NULL COMMENT '租借结束时间',
    `lin_shi_zhang_hao`       varchar(255)  DEFAULT NULL COMMENT '临时账号',
    `lin_shi_mi_ma`           varchar(255)  DEFAULT NULL COMMENT '临时密码',
    `fu_kuan_shi_jian`        datetime(6)   DEFAULT NULL COMMENT '付款时间',
    `queren_shou_huo_shi_jian` datetime(6)  DEFAULT NULL COMMENT '确认收货时间',
    `ju_jue_yuan_yin`         varchar(500)  DEFAULT NULL COMMENT '拒绝原因',
    `ip`                      varchar(50)   DEFAULT NULL COMMENT 'IP地址',
    `she_bei_shi_wen`         varchar(500)  DEFAULT NULL COMMENT '设备指纹',
    `chuang_jian_shi_jian`    datetime(6)   NOT NULL COMMENT '创建时间',
    `geng_xin_shi_jian`       datetime(6)   DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ding_dan_hao` (`ding_dan_hao`),
    KEY `idx_shang_pin_id` (`shang_pin_id`),
    KEY `idx_mai_jia_id` (`mai_jia_id`),
    KEY `idx_mai_jia_id2` (`mai_jia_id2`),
    KEY `idx_zhuang_tai` (`zhuang_tai`),
    KEY `idx_ip` (`ip`),
    KEY `idx_she_bei_shi_wen` (`she_bei_shi_wen`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ------------------------------------------------------------
-- 风控规则表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `fengkong_rule` (
    `id`              bigint       NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    `rule_code`       varchar(50)  NOT NULL COMMENT '规则代码',
    `rule_name`       varchar(100) NOT NULL COMMENT '规则名称',
    `rule_type`       varchar(255) NOT NULL COMMENT '规则类型：FREQUENCY-频率限制，BEHAVIOR-行为检测，BLACKLIST-黑名单，AMOUNT-金额检测',
    `risk_score`      int          NOT NULL COMMENT '风险分值',
    `threshold_value` int          NOT NULL COMMENT '阈值',
    `time_window`     int          NOT NULL COMMENT '时间窗口（分钟）',
    `enabled`         bit(1)       NOT NULL COMMENT '是否启用：0-禁用，1-启用',
    `description`     varchar(200) DEFAULT NULL COMMENT '规则描述',
    `create_time`     datetime(6)  NOT NULL COMMENT '创建时间',
    `update_time`     datetime(6)  NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_rule_code` (`rule_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风控规则表';

-- ------------------------------------------------------------
-- 评价表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `pingjia` (
    `id`                 bigint      NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    `ding_dan_id`        bigint      NOT NULL COMMENT '订单ID',
    `shang_pin_id`       bigint      NOT NULL COMMENT '商品ID',
    `ping_jia_ren_id`    bigint      NOT NULL COMMENT '评价人ID',
    `ping_fen`           int         NOT NULL COMMENT '评分：1-5',
    `ping_jia_nei_rong`  text COMMENT '评价内容',
    `chuang_jian_shi_jian` datetime(6) NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_ding_dan_id` (`ding_dan_id`),
    KEY `idx_shang_pin_id` (`shang_pin_id`),
    KEY `idx_ping_jia_ren_id` (`ping_jia_ren_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- ------------------------------------------------------------
-- 消息表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `xiaoxi` (
    `id`                   bigint       NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `yong_hu_id`           bigint       NOT NULL COMMENT '收件人ID',
    `biao_ti`              varchar(100) NOT NULL COMMENT '消息标题',
    `nei_rong`             text COMMENT '消息内容',
    `lei_xing`             int          NOT NULL COMMENT '类型：0-系统消息，1-订单消息，2-商品消息',
    `shi_fou_yi_du`        int          NOT NULL COMMENT '是否已读：0-未读，1-已读',
    `chuang_jian_shi_jian` datetime(6)  NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_yong_hu_id` (`yong_hu_id`),
    KEY `idx_shi_fou_yi_du` (`shi_fou_yi_du`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- ------------------------------------------------------------
-- 风险记录表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `fengxian` (
    `id`                   bigint      NOT NULL AUTO_INCREMENT COMMENT '风险记录ID',
    `yong_hu_id`           bigint      NOT NULL COMMENT '用户ID',
    `feng_xian_lei_xing`   varchar(50) NOT NULL COMMENT '风险类型',
    `miao_shu`             text COMMENT '风险描述',
    `ip`                   varchar(50) DEFAULT NULL COMMENT 'IP地址',
    `she_bei_shi_wen`      varchar(500) DEFAULT NULL COMMENT '设备指纹',
    `chuang_jian_shi_jian` datetime(6) NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_yong_hu_id` (`yong_hu_id`),
    KEY `idx_feng_xian_lei_xing` (`feng_xian_lei_xing`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风险记录表';

-- ------------------------------------------------------------
-- 管理员操作日志表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `guanliyuan` (
    `id`                   bigint      NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `guan_li_yuan_id`      bigint      NOT NULL COMMENT '管理员用户ID',
    `cao_zuo`              varchar(50) NOT NULL COMMENT '操作类型',
    `mu_biao_lei_xing`     varchar(100) DEFAULT NULL COMMENT '目标类型',
    `mu_biao_id`           bigint      DEFAULT NULL COMMENT '目标ID',
    `xi_qing`              text COMMENT '操作详情',
    `chuang_jian_shi_jian` datetime(6) NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_guan_li_yuan_id` (`guan_li_yuan_id`),
    KEY `idx_cao_zuo` (`cao_zuo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员操作日志表';

-- ------------------------------------------------------------
-- IM 会话表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `im_hui_hua` (
    `id`                      bigint      NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    `hui_hua_hao`             varchar(50) NOT NULL COMMENT '会话号',
    `yong_hu_id1`             bigint      NOT NULL COMMENT '用户1',
    `yong_hu_id2`             bigint      NOT NULL COMMENT '用户2',
    `zui_hou_xiao_xi_shi_jian` datetime(6) DEFAULT NULL COMMENT '最后一条消息时间',
    `chuang_jian_shi_jian`    datetime(6) NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_hui_hua_hao` (`hui_hua_hao`),
    KEY `idx_yong_hu_id1` (`yong_hu_id1`),
    KEY `idx_yong_hu_id2` (`yong_hu_id2`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IM会话表';

-- ------------------------------------------------------------
-- IM 消息表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `im_xiao_xi` (
    `id`                bigint      NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `hui_hua_hao`       varchar(50) NOT NULL COMMENT '会话号',
    `fa_song_ren_id`    bigint      NOT NULL COMMENT '发送人ID',
    `shou_dao_ren_id`   bigint      NOT NULL COMMENT '接收人ID',
    `nei_rong`          text COMMENT '消息内容',
    `xiao_xi_lei_xing`  varchar(20) NOT NULL COMMENT '消息类型',
    `shi_fou_yi_du`     int         NOT NULL COMMENT '是否已读：0-未读，1-已读',
    `fa_song_shi_jian`  datetime(6) NOT NULL COMMENT '发送时间',
    PRIMARY KEY (`id`),
    KEY `idx_hui_hua_hao` (`hui_hua_hao`),
    KEY `idx_fa_song_shi_jian` (`fa_song_shi_jian`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IM消息表';

-- ------------------------------------------------------------
-- 浏览历史表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `liu_lan_li_shi` (
    `id`              bigint      NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `yong_hu_id`      bigint      NOT NULL COMMENT '用户ID',
    `shang_pin_id`    bigint      NOT NULL COMMENT '商品ID',
    `liu_lan_shi_jian` datetime(6) NOT NULL COMMENT '浏览时间',
    PRIMARY KEY (`id`),
    KEY `idx_yong_hu_id` (`yong_hu_id`),
    KEY `idx_shang_pin_id` (`shang_pin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='浏览历史表';

-- ------------------------------------------------------------
-- 轮播图表
-- 注意：tie_tle 是实体字段 tieTle 的真实映射结果（实体中的拼写如此），
--       此处保持一致，避免与实体不匹配。
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `lunbotu` (
    `id`                   bigint       NOT NULL AUTO_INCREMENT COMMENT '轮播图ID',
    `tu_pian_url`          varchar(255) NOT NULL COMMENT '图片URL',
    `tie_tle`              varchar(255) DEFAULT NULL COMMENT '标题',
    `lian_jie_url`         varchar(255) DEFAULT NULL COMMENT '跳转链接',
    `shun_xu`              int          DEFAULT NULL COMMENT '排序',
    `zhuang_tai`           int          DEFAULT NULL COMMENT '状态：0-禁用，1-启用',
    `chuang_jian_shi_jian` datetime(6)  DEFAULT NULL COMMENT '创建时间',
    `geng_xin_shi_jian`    datetime(6)  DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图表';

-- ------------------------------------------------------------
-- 钱包表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `qian_bao` (
    `id`                   bigint        NOT NULL AUTO_INCREMENT COMMENT '钱包ID',
    `yong_hu_id`           bigint        NOT NULL COMMENT '用户ID',
    `yue`                  decimal(38,2) NOT NULL COMMENT '余额',
    `ke_ti_xian_jine`      decimal(38,2) NOT NULL COMMENT '可提现金额',
    `chuang_jian_shi_jian` datetime(6)   NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_qian_bao_yong_hu_id` (`yong_hu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='钱包表';

-- ------------------------------------------------------------
-- 收藏表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `shou_cang` (
    `id`                   bigint      NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `yong_hu_id`           bigint      NOT NULL COMMENT '用户ID',
    `shang_pin_id`         bigint      NOT NULL COMMENT '商品ID',
    `chuang_jian_shi_jian` datetime(6) NOT NULL COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    KEY `idx_yong_hu_id` (`yong_hu_id`),
    KEY `idx_shang_pin_id` (`shang_pin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- ------------------------------------------------------------
-- 提现申请表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `ti_xian_shen_qing` (
    `id`                 bigint        NOT NULL AUTO_INCREMENT COMMENT '申请ID',
    `ti_xian_hao`        varchar(50)   NOT NULL COMMENT '提现单号',
    `yong_hu_id`         bigint        NOT NULL COMMENT '用户ID',
    `ti_xian_jine`       decimal(38,2) NOT NULL COMMENT '提现金额',
    `shou_kuan_fang_shi` varchar(20)   NOT NULL COMMENT '收款方式',
    `shou_kuan_zhang_hao` varchar(100) NOT NULL COMMENT '收款账号',
    `shou_kuan_ren`      varchar(50)   DEFAULT NULL COMMENT '收款人',
    `zhuang_tai`         int           NOT NULL COMMENT '状态：0-待处理，1-已打款，2-已拒绝',
    `fail_reason`        varchar(500)  DEFAULT NULL COMMENT '失败原因',
    `shen_qing_shi_jian` datetime(6)   NOT NULL COMMENT '申请时间',
    `chu_li_shi_jian`    datetime(6)   DEFAULT NULL COMMENT '处理时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ti_xian_hao` (`ti_xian_hao`),
    KEY `idx_yong_hu_id` (`yong_hu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提现申请表';

-- ------------------------------------------------------------
-- 退款申请表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tui_kuan_shen_qing` (
    `id`                   bigint        NOT NULL AUTO_INCREMENT COMMENT '申请ID',
    `ding_dan_hao`         varchar(50)   NOT NULL COMMENT '订单号',
    `yong_hu_id`           bigint        NOT NULL COMMENT '申请人ID',
    `tui_kuan_jine`        decimal(38,2) NOT NULL COMMENT '退款金额',
    `tui_kuan_yuan_yin`    text COMMENT '退款原因',
    `ping_zheng_url`       varchar(500)  DEFAULT NULL COMMENT '凭证URL',
    `mai_jia_fu_kuan`      int           DEFAULT NULL COMMENT '是否卖家付款',
    `zhuang_tai`           int           NOT NULL COMMENT '状态：0-待处理，1-已退款，2-已拒绝',
    `guan_li_yuan_bei_zhu` text COMMENT '管理员备注',
    `shen_qing_shi_jian`   datetime(6)   NOT NULL COMMENT '申请时间',
    `chu_li_shi_jian`      datetime(6)   DEFAULT NULL COMMENT '处理时间',
    PRIMARY KEY (`id`),
    KEY `idx_ding_dan_hao` (`ding_dan_hao`),
    KEY `idx_yong_hu_id` (`yong_hu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款申请表';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 风控规则
INSERT INTO `fengkong_rule` (`rule_code`, `rule_name`, `rule_type`, `risk_score`, `threshold_value`, `time_window`, `enabled`, `description`, `create_time`, `update_time`) VALUES
('IP_FREQUENCY',        'IP频率限制',     'FREQUENCY', 20,  5,    60, 1, '同一IP在60分钟内下单超过5次',        NOW(6), NOW(6)),
('DEVICE_FREQUENCY',    '设备频率限制',   'FREQUENCY', 20,  3,    60, 1, '同一设备在60分钟内下单超过3次',      NOW(6), NOW(6)),
('USER_FREQUENCY',      '用户下单频率',   'FREQUENCY', 15,  10,   60, 1, '用户在60分钟内下单超过10次',        NOW(6), NOW(6)),
('HIGH_RISK_USER',      '高风险用户',     'BEHAVIOR',  30,  3,    0,  1, '用户有3次以上风险记录',             NOW(6), NOW(6)),
('BANNED_USER',         '封禁用户',       'BLACKLIST', 100, 0,    0,  1, '用户已被封禁',                      NOW(6), NOW(6)),
('LARGE_AMOUNT',        '大额交易',       'AMOUNT',    25,  1000, 0,  1, '单笔订单金额超过1000元',            NOW(6), NOW(6)),
('SHORT_TERM_FREQUENT', '短时间频繁租用', 'BEHAVIOR',  20,  2,    60, 1, '1小时内对同一商品下单超过2次',      NOW(6), NOW(6));

-- 管理员账号（用户名 admin / 密码 admin123）
-- 该哈希由 Spring Security BCryptPasswordEncoder 生成并已用 matches("admin123", hash) 验证通过。
INSERT INTO `yonghu` (`yong_hu_ming`, `mi_ma`, `shou_ji_hao`, `jue_se`, `shi_fou_shi_ming`, `zhuang_tai`, `chuang_jian_shi_jian`, `geng_xin_shi_jian`)
VALUES ('admin', '$2a$10$xfFnQUBV2Lg6Hh/Pau5te.A.Yi2LsC9P5qvMjOgdcOorrnhzOUnOm', '13800138000', 2, 1, 0, NOW(6), NOW(6));

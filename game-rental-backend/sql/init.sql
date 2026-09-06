-- 三角洲行动游戏账号租赁平台数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS sanJiaoZhouXingDong
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE sanJiaoZhouXingDong;

-- 用户表
CREATE TABLE IF NOT EXISTS yonghu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    yongHuMing VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    miMa VARCHAR(100) NOT NULL COMMENT '密码',
    shouJiHao VARCHAR(11) NOT NULL UNIQUE COMMENT '手机号',
    zhenShiXingMing VARCHAR(50) COMMENT '真实姓名',
    shenFenZheng VARCHAR(18) COMMENT '身份证号',
    shiFouShiMing INT NOT NULL DEFAULT 0 COMMENT '是否实名：0-未实名，1-已实名',
    jueSe INT NOT NULL DEFAULT 0 COMMENT '角色：0-普通用户，1-卖家，2-管理员',
    zhuangTai INT NOT NULL DEFAULT 0 COMMENT '状态：0-正常，1-封禁',
    openId VARCHAR(100) COMMENT '第三方登录OpenID',
    openIdType VARCHAR(20) COMMENT '第三方登录类型：wechat-微信，qq-QQ',
    chuangJianShiJian DATETIME NOT NULL COMMENT '创建时间',
    gengXinShiJian DATETIME COMMENT '更新时间',
    INDEX idx_yongHuMing (yongHuMing),
    INDEX idx_shouJiHao (shouJiHao),
    INDEX idx_openId (openId, openIdType)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 商品表
CREATE TABLE IF NOT EXISTS shangpin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID',
    maiJiaId BIGINT NOT NULL COMMENT '卖家用户ID',
    biaoTi VARCHAR(100) NOT NULL COMMENT '商品标题',
    miaoShu TEXT COMMENT '商品描述',
    youXiZhangHao VARCHAR(100) COMMENT '游戏账号',
    youXiMiMa VARCHAR(100) COMMENT '游戏密码',
    riZuJiaGe DECIMAL(10,2) NOT NULL COMMENT '日租价格',
    zuiShaoZuJieTianShu INT NOT NULL DEFAULT 1 COMMENT '最少租借天数',
    fengXianDengJi VARCHAR(20) COMMENT '风险等级',
    zhuangTai INT NOT NULL DEFAULT 0 COMMENT '状态：0-待审核，1-上架，2-已借出，3-下架',
    zuJieCiShu INT NOT NULL DEFAULT 0 COMMENT '租借次数',
    faBuShiJian DATETIME NOT NULL COMMENT '发布时间',
    chuangJianShiJian DATETIME NOT NULL COMMENT '创建时间',
    gengXinShiJian DATETIME COMMENT '更新时间',
    INDEX idx_maiJiaId (maiJiaId),
    INDEX idx_zhuangTai (zhuangTai),
    FULLTEXT INDEX ft_shangpin (biaoTi, miaoShu, youXiZhangHao)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 订单表
CREATE TABLE IF NOT EXISTS dingdan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    dingDanHao VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',
    shangPinId BIGINT NOT NULL COMMENT '商品ID',
    maiJiaId BIGINT NOT NULL COMMENT '卖家用户ID',
    maiJiaId2 BIGINT NOT NULL COMMENT '买家用户ID',
    zuJieTianShu INT NOT NULL COMMENT '租借天数',
    zongJinE DECIMAL(10,2) NOT NULL COMMENT '总金额',
    zhuangTai INT NOT NULL DEFAULT 0 COMMENT '状态：0-待支付，1-已支付，2-已完成，3-已取消，4-纠纷中',
    zuJieKaiShiShiJian DATETIME COMMENT '租借开始时间',
    zuJieJieShuShiJian DATETIME COMMENT '租借结束时间',
    linShiZhangHao VARCHAR(100) COMMENT '临时账号',
    linShiMiMa VARCHAR(100) COMMENT '临时密码',
    fuKuanShiJian DATETIME COMMENT '付款时间',
    ip VARCHAR(50) COMMENT 'IP地址',
    sheBeiShiWen VARCHAR(500) COMMENT '设备指纹',
    chuangJianShiJian DATETIME NOT NULL COMMENT '创建时间',
    gengXinShiJian DATETIME COMMENT '更新时间',
    INDEX idx_dingDanHao (dingDanHao),
    INDEX idx_shangPinId (shangPinId),
    INDEX idx_maiJiaId (maiJiaId),
    INDEX idx_maiJiaId2 (maiJiaId2),
    INDEX idx_zhuangTai (zhuangTai),
    INDEX idx_ip (ip),
    INDEX idx_sheBeiShiWen (sheBeiShiWen)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 风控规则表
CREATE TABLE IF NOT EXISTS fengkong_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '规则ID',
    rule_code VARCHAR(50) NOT NULL UNIQUE COMMENT '规则代码',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_type VARCHAR(50) NOT NULL COMMENT '规则类型：FREQUENCY-频率限制，BEHAVIOR-行为检测，BLACKLIST-黑名单，AMOUNT-金额检测',
    risk_score INT NOT NULL COMMENT '风险分值',
    threshold_value INT NOT NULL COMMENT '阈值',
    time_window INT NOT NULL COMMENT '时间窗口（分钟）',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    description VARCHAR(200) COMMENT '规则描述',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控规则表';

-- 初始化风控规则
INSERT INTO fengkong_rule (rule_code, rule_name, rule_type, risk_score, threshold_value, time_window, enabled, description, create_time, update_time) VALUES
('IP_FREQUENCY', 'IP频率限制', 'FREQUENCY', 20, 5, 60, 1, '同一IP在60分钟内下单超过5次', NOW(), NOW()),
('DEVICE_FREQUENCY', '设备频率限制', 'FREQUENCY', 20, 3, 60, 1, '同一设备在60分钟内下单超过3次', NOW(), NOW()),
('USER_FREQUENCY', '用户下单频率', 'FREQUENCY', 15, 10, 60, 1, '用户在60分钟内下单超过10次', NOW(), NOW()),
('HIGH_RISK_USER', '高风险用户', 'BEHAVIOR', 30, 3, 0, 1, '用户有3次以上风险记录', NOW(), NOW()),
('BANNED_USER', '封禁用户', 'BLACKLIST', 100, 0, 0, 1, '用户已被封禁', NOW(), NOW()),
('LARGE_AMOUNT', '大额交易', 'AMOUNT', 25, 1000, 0, 1, '单笔订单金额超过1000元', NOW(), NOW()),
('SHORT_TERM_FREQUENT', '短时间频繁租用', 'BEHAVIOR', 20, 2, 60, 1, '1小时内对同一商品下单超过2次', NOW(), NOW());

-- 评价表
CREATE TABLE IF NOT EXISTS pingjia (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评价ID',
    dingDanId BIGINT NOT NULL COMMENT '订单ID',
    shangPinId BIGINT NOT NULL COMMENT '商品ID',
    maiJiaId BIGINT NOT NULL COMMENT '卖家用户ID',
    pingJiaRenId BIGINT NOT NULL COMMENT '评价人ID',
    pingFen INT NOT NULL COMMENT '评分：1-5',
    pingJiaNeiRong TEXT COMMENT '评价内容',
    pingJiaShiJian DATETIME NOT NULL COMMENT '评价时间',
    INDEX idx_dingDanId (dingDanId),
    INDEX idx_shangPinId (shangPinId),
    INDEX idx_pingJiaRenId (pingJiaRenId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- 消息表
CREATE TABLE IF NOT EXISTS xiaoxi (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    shouJianRenId BIGINT NOT NULL COMMENT '收件人ID',
    faSongRenId BIGINT COMMENT '发件人ID',
    biaoTi VARCHAR(100) COMMENT '消息标题',
    neiRong TEXT COMMENT '消息内容',
    leiXing INT NOT NULL DEFAULT 0 COMMENT '类型：0-系统消息，1-订单消息，2-商品消息',
    shiFouDuGuo INT NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    faSongShiJian DATETIME NOT NULL COMMENT '发送时间',
    INDEX idx_shouJianRenId (shouJianRenId),
    INDEX idx_shiFouDuGuo (shiFouDuGuo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- 风险表
CREATE TABLE IF NOT EXISTS fengxian (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '风险记录ID',
    yongHuId BIGINT NOT NULL COMMENT '用户ID',
    fengXianLeiXing VARCHAR(50) NOT NULL COMMENT '风险类型',
    miaoShu TEXT COMMENT '风险描述',
    ip VARCHAR(50) COMMENT 'IP地址',
    sheBeiShiWen VARCHAR(500) COMMENT '设备指纹',
    chuangJianShiJian DATETIME NOT NULL COMMENT '创建时间',
    INDEX idx_yongHuId (yongHuId),
    INDEX idx_fengXianLeiXing (fengXianLeiXing)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风险表';

-- 管理员日志表
CREATE TABLE IF NOT EXISTS guanliyuan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    guanLiYuanId BIGINT NOT NULL COMMENT '管理员用户ID',
    caoZuo VARCHAR(50) NOT NULL COMMENT '操作类型',
    muBiaoLeiXing VARCHAR(50) COMMENT '目标类型',
    muBiaoId BIGINT COMMENT '目标ID',
    xiQing TEXT COMMENT '操作详情',
    chuangJianShiJian DATETIME NOT NULL COMMENT '创建时间',
    INDEX idx_guanLiYuanId (guanLiYuanId),
    INDEX idx_caoZuo (caoZuo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员日志表';

-- 初始化管理员账号（密码：admin123）
INSERT INTO yonghu (yongHuMing, miMa, shouJiHao, jueSe, shiFouShiMing, chuangJianShiJian, gengXinShiJian)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '13800138000', 2, 1, NOW(), NOW());

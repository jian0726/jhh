package com.delta.rental.enums;

public enum ThirdPartyLoginType {
    WECHAT("wechat", "微信"),
    QQ("qq", "QQ"),
    ALIPAY("alipay", "支付宝");

    private final String code;
    private final String name;

    ThirdPartyLoginType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static ThirdPartyLoginType fromCode(String code) {
        for (ThirdPartyLoginType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的第三方登录类型: " + code);
    }
}
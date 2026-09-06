package com.delta.rental.service;

import com.delta.rental.config.ThirdPartyConfig;
//import com.delta.rental.dto.ThirdPartyLoginRequest;
import com.delta.rental.entity.YongHu;
import com.delta.rental.enums.ThirdPartyLoginType;
import com.delta.rental.exception.BusinessException;
import com.delta.rental.repository.YongHuRepository;
import com.delta.rental.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThirdPartyLoginService {

    private final YongHuRepository yongHuRepository;
    private final ThirdPartyConfig thirdPartyConfig;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate = new RestTemplate();

    public String getAuthUrl(ThirdPartyLoginType type, String state) {
        switch (type) {
            case WECHAT:
                return getWechatAuthUrl(state);
            case QQ:
                return getQQAuthUrl(state);
            default:
                throw new BusinessException(400, "不支持的登录方式");
        }
    }

    private String getWechatAuthUrl(String state) {
        return String.format(
                "https://open.weixin.qq.com/connect/qrconnect?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_login&state=%s#wechat_redirect",
                thirdPartyConfig.getWechat().getAppId(),
                thirdPartyConfig.getWechat().getRedirectUri(),
                state
        );
    }

    private String getQQAuthUrl(String state) {
        return String.format(
                "https://graph.qq.com/oauth2.0/authorize?response_type=code&client_id=%s&redirect_uri=%s&state=%s&scope=get_user_info",
                thirdPartyConfig.getQq().getAppId(),
                thirdPartyConfig.getQq().getRedirectUri(),
                state
        );
    }

    public Map<String, Object> login(ThirdPartyLoginType type, String code, String state) {
        String openId = getOpenId(type, code);
        
        YongHu user = yongHuRepository.findByOpenIdAndOpenIdType(openId, type.getCode())
                .orElseGet(() -> {
                    YongHu newUser = new YongHu();
                    newUser.setYongHuMing(generateUsername(type));
                    newUser.setMiMa(UUID.randomUUID().toString());
                    newUser.setShouJiHao("");
                    newUser.setOpenId(openId);
                    newUser.setOpenIdType(type.getCode());
                    newUser.setJueSe(0);
                    newUser.setZhuangTai(0);
                    newUser.setShiFouShiMing(0);
                    return yongHuRepository.save(newUser);
                });

        if (user.getZhuangTai() == 1) {
            throw new BusinessException(403, "账号已被封禁");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getYongHuMing(), user.getJueSe());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("yongHu", user);
        
        return result;
    }

    private String getOpenId(ThirdPartyLoginType type, String code) {
        try {
            switch (type) {
                case WECHAT:
                    return getWechatOpenId(code);
                case QQ:
                    return getQQOpenId(code);
                default:
                    throw new BusinessException(400, "不支持的登录方式");
            }
        } catch (Exception e) {
            log.error("获取第三方OpenId失败", e);
            throw new BusinessException(500, "第三方登录失败: " + e.getMessage());
        }
    }

    private String getWechatOpenId(String code) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                thirdPartyConfig.getWechat().getAppId(),
                thirdPartyConfig.getWechat().getAppSecret(),
                code
        );
        
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map<String, Object> body = response.getBody();
        
        if (body != null && body.containsKey("openid")) {
            return (String) body.get("openid");
        }
        
        throw new BusinessException(500, "获取微信OpenId失败");
    }

    private String getQQOpenId(String code) {
        String tokenUrl = String.format(
                "https://graph.qq.com/oauth2.0/token?grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s",
                thirdPartyConfig.getQq().getAppId(),
                thirdPartyConfig.getQq().getAppKey(),
                code,
                thirdPartyConfig.getQq().getRedirectUri()
        );
        
        ResponseEntity<String> tokenResponse = restTemplate.getForEntity(tokenUrl, String.class);
        String tokenBody = tokenResponse.getBody();
        
        String accessToken = extractValue(tokenBody, "access_token");
        
        String openIdUrl = String.format(
                "https://graph.qq.com/oauth2.0/me?access_token=%s",
                accessToken
        );
        
        ResponseEntity<String> openIdResponse = restTemplate.getForEntity(openIdUrl, String.class);
        String openIdBody = openIdResponse.getBody();
        
        return extractValue(openIdBody, "openid");
    }

    private String extractValue(String body, String key) {
        String pattern = "\"" + key + "\":\"([^\"]+)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(body);
        if (m.find()) {
            return m.group(1);
        }
        throw new BusinessException(500, "解析" + key + "失败");
    }

    private String generateUsername(ThirdPartyLoginType type) {
        String prefix = type.getName();
        String random = UUID.randomUUID().toString().substring(0, 8);
        return prefix + "_" + random;
    }
}
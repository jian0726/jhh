package com.delta.rental.service;

import com.delta.rental.dto.DengLuRequest;
import com.delta.rental.dto.ZhuCeRequest;
import com.delta.rental.entity.YongHu;
import com.delta.rental.exception.BusinessException;
import com.delta.rental.repository.YongHuRepository;
import com.delta.rental.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class YongHuService {

    private final YongHuRepository yongHuRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CaptchaService captchaService;

    public Map<String, Object> zhuCe(ZhuCeRequest request) {
        // 邮箱注册模式
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (yongHuRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException(400, "邮箱已注册");
            }
            if (request.getVerifyCode() == null || request.getVerifyCode().isBlank()) {
                throw new BusinessException(400, "请输入验证码");
            }
            String key = "email:" + request.getEmail();
            if (!captchaService.validateCaptcha(key, request.getVerifyCode())) {
                throw new BusinessException(400, "验证码错误或已过期");
            }

            YongHu yongHu = new YongHu();
            // 从邮箱自动生成用户名
            String generatedUsername = request.getEmail().split("@")[0] +
                Integer.toString((int)(Math.random() * 90000 + 10000));
            yongHu.setYongHuMing(generatedUsername);
            yongHu.setMiMa(passwordEncoder.encode(request.getMiMa()));
            yongHu.setEmail(request.getEmail());
            yongHu.setShouJiHao(String.format("em%08d", Math.abs(request.getEmail().hashCode() % 100000000)));
            yongHu.setJueSe(0);
            yongHu = yongHuRepository.save(yongHu);

            String token = jwtUtil.generateToken(yongHu.getId(), yongHu.getYongHuMing(), yongHu.getJueSe());
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "注册成功");
            result.put("token", token);
            result.put("user", toUserVo(yongHu));
            result.put("yongHu", toUserVo(yongHu));
            return result;
        }

        // 手机号注册模式（原有逻辑 + 验证码校验）
        if (request.getYongHuMing() == null || request.getYongHuMing().isBlank()) {
            throw new BusinessException(400, "用户名不能为空");
        }
        if (request.getShouJiHao() == null || request.getShouJiHao().isBlank()) {
            throw new BusinessException(400, "手机号不能为空");
        }
        // SMS验证码校验
        if (request.getVerifyCode() != null && !request.getVerifyCode().isBlank()) {
            String smsKey = "sms:" + request.getShouJiHao();
            if (!captchaService.validateCaptcha(smsKey, request.getVerifyCode())) {
                throw new BusinessException(400, "短信验证码错误或已过期");
            }
        }
        if (yongHuRepository.existsByYongHuMing(request.getYongHuMing())) {
            throw new BusinessException(400, "用户名已存在");
        }
        if (yongHuRepository.existsByShouJiHao(request.getShouJiHao())) {
            throw new BusinessException(400, "手机号已存在");
        }

        YongHu yongHu = new YongHu();
        yongHu.setYongHuMing(request.getYongHuMing());
        yongHu.setMiMa(passwordEncoder.encode(request.getMiMa()));
        yongHu.setShouJiHao(request.getShouJiHao());
        yongHu.setJueSe(0);
        yongHu = yongHuRepository.save(yongHu);

        String token = jwtUtil.generateToken(yongHu.getId(), yongHu.getYongHuMing(), yongHu.getJueSe());
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "注册成功");
        result.put("token", token);
        result.put("user", toUserVo(yongHu));
        result.put("yongHu", toUserVo(yongHu));
        return result;
    }

    public Map<String, Object> dengLu(DengLuRequest request) {
        // 先用用户名查找，找不到用手机号查找，再找不到用邮箱查找
        YongHu yongHu = yongHuRepository.findByYongHuMing(request.getYongHuMing())
                .orElseGet(() -> yongHuRepository.findByShouJiHao(request.getYongHuMing())
                        .orElseGet(() -> yongHuRepository.findByEmail(request.getYongHuMing())
                                .orElseThrow(() -> new BusinessException(400, "用户名或密码错误"))));

        if (!passwordEncoder.matches(request.getMiMa(), yongHu.getMiMa())) {
            // 业务校验失败使用 400，避免前端拦截器把登录失败误判为“登录态失效”而跳转首页
            throw new BusinessException(400, "用户名或密码错误");
        }

        if (yongHu.getZhuangTai() == 1) {
            throw new BusinessException(403, "账号已被封禁");
        }

        String token = jwtUtil.generateToken(yongHu.getId(), yongHu.getYongHuMing(), yongHu.getJueSe());
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "登录成功");
        result.put("token", token);
        result.put("user", toUserVo(yongHu));
        result.put("yongHu", toUserVo(yongHu));
        return result;
    }

    /**
     * 转换为前端使用的用户信息结构（脱敏，不返回密码）
     */
    public Map<String, Object> toUserVo(YongHu yongHu) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", yongHu.getId());
        vo.put("yongHuMing", yongHu.getYongHuMing());
        vo.put("shouJiHao", yongHu.getShouJiHao());
        vo.put("email", yongHu.getEmail());
        vo.put("jueSe", yongHu.getJueSe());
        vo.put("shiFouShiMing", yongHu.getShiFouShiMing());
        return vo;
    }
    
    public YongHu getYongHuById(Long id) {
        return yongHuRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
    }
    
    public YongHu shiMingRenZheng(Long yongHuId, String zhenShiXingMing, String shenFenZheng) {
        YongHu yongHu = getYongHuById(yongHuId);
        yongHu.setZhenShiXingMing(zhenShiXingMing);
        yongHu.setShenFenZheng(shenFenZheng);
        yongHu.setShiFouShiMing(1);
        return yongHuRepository.save(yongHu);
    }
    
    public YongHu updateYongHuJueSe(Long yongHuId, Integer jueSe) {
        YongHu yongHu = getYongHuById(yongHuId);
        yongHu.setJueSe(jueSe);
        return yongHuRepository.save(yongHu);
    }
    
    public YongHu fengJinYongHu(Long yongHuId) {
        YongHu yongHu = getYongHuById(yongHuId);
        yongHu.setZhuangTai(1);
        return yongHuRepository.save(yongHu);
    }
    
    public YongHu jieFengYongHu(Long yongHuId) {
        YongHu yongHu = getYongHuById(yongHuId);
        yongHu.setZhuangTai(0);
        return yongHuRepository.save(yongHu);
    }
    
    public YongHu updateYongHuInfo(Long yongHuId, String yongHuMing, String shouJiHao) {
        YongHu yongHu = getYongHuById(yongHuId);
        if (yongHuMing != null && !yongHuMing.isEmpty()) {
            if (!yongHu.getYongHuMing().equals(yongHuMing) && yongHuRepository.existsByYongHuMing(yongHuMing)) {
                throw new BusinessException(400, "用户名已存在");
            }
            yongHu.setYongHuMing(yongHuMing);
        }
        if (shouJiHao != null && !shouJiHao.isEmpty()) {
            if (!yongHu.getShouJiHao().equals(shouJiHao) && yongHuRepository.existsByShouJiHao(shouJiHao)) {
                throw new BusinessException(400, "手机号已存在");
            }
            yongHu.setShouJiHao(shouJiHao);
        }
        return yongHuRepository.save(yongHu);
    }
    
    public void updateMiMa(Long yongHuId, String oldPassword, String newPassword) {
        YongHu yongHu = getYongHuById(yongHuId);
        if (!passwordEncoder.matches(oldPassword, yongHu.getMiMa())) {
            throw new BusinessException(400, "原密码错误");
        }
        yongHu.setMiMa(passwordEncoder.encode(newPassword));
        yongHuRepository.save(yongHu);
    }
    
    public Map<String, Object> getShiMingStatus(Long yongHuId) {
        YongHu yongHu = getYongHuById(yongHuId);
        Map<String, Object> status = new HashMap<>();
        status.put("shiFouShiMing", yongHu.getShiFouShiMing());
        status.put("zhenShiXingMing", yongHu.getZhenShiXingMing());
        return status;
    }
}

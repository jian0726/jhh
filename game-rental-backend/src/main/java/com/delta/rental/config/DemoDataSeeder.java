package com.delta.rental.config;

import com.delta.rental.entity.LunBoTu;
import com.delta.rental.entity.ShangPin;
import com.delta.rental.repository.LunBoTuRepository;
import com.delta.rental.repository.ShangPinRepository;
import com.delta.rental.service.FileStorageService;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 演示数据种子（仅用于本地开发/答辩演示）。
 *
 * <p>作用：把「前端原本放在 qianduan/img 里的示例图」迁移为<b>后端托管</b>——
 * 图片随包发布在 {@code resources/demo-images} 下，启动时复制到上传目录，
 * 再把商品/轮播记录写进数据库（图片字段只存 URL）。这样前端关闭 mock 后依然有数据可展示。
 *
 * <p>三条安全约束：
 * <ol>
 *   <li>由 {@code app.seed-demo} 开关控制，默认开启，设为 false 即完全关闭；</li>
 *   <li><b>只在表为空时写入</b>，不会覆盖或重复插入你已有的数据；</li>
 *   <li>图片已存在时跳过复制，不重复占用磁盘。</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DemoDataSeeder implements CommandLineRunner {

    /** 随包发布的演示图片文件名（对应 resources/demo-images/*.png） */
    private static final String[] DEMO_IMAGES = {
            "demo-1.png", "demo-2.png", "demo-3.png", "demo-4.png", "demo-5.png"
    };

    private static final String[] RANKS = {
            "青铜", "白银", "黄金", "铂金", "钻石", "黑鹰"
    };

    private static final String[] SAFE_BOXES = {
            "无安全箱", "普通安全箱", "中级安全箱2*2", "顶级安全箱3*3"
    };

    private static final String[][] KNIFE_SKINS = {
            {"北极星"}, {"暗星"}, {"坠星者"}, {"信条"}, {"蝴蝶刀-狩猎"}
    };

    private static final String[][] OPERATOR_SKINS = {
            {"红狼-蚀金玫瑰"}, {"威龙-蛟龙特战队"}, {"露娜-黑天际线"}, {"蜂医-危险物质"}
    };

    private static final String[][] WEAPON_SKINS = {
            {"KC17造物纪元", "QBZ95王牌之剑"},
            {"M250电玩S2", "MP7电玩S2"},
            {"ASVal悬赏令", "腾龙气象感应"},
            {"SCAR电玩高手", "Vector美杜莎"}
    };

    /** 演示商品条数 */
    private static final int DEMO_PRODUCT_COUNT = 8;

    private final ShangPinRepository shangPinRepository;
    private final LunBoTuRepository lunBoTuRepository;

    private final FileStorageService fileStorageService;

    private final Gson gson = new Gson();

    /** 是否写入演示数据 */
    @Value("${app.seed-demo:true}")
    private boolean seedDemo;

    /** 演示数据归属的卖家用户 id（默认 admin=1） */
    @Value("${app.seed-demo-mai-jia-id:1}")
    private Long maiJiaId;

    @Override
    public void run(String... args) {
        if (!seedDemo) {
            return;
        }
        // 演示数据属于「锦上添花」，任何一步失败都不应影响应用启动
        try {
            copyDemoImagesToUploadDir();
        } catch (Exception e) {
            log.warn("[DemoDataSeeder] 复制演示图片失败：{}", e.getMessage());
        }
        try {
            seedProductsIfEmpty();
        } catch (Exception e) {
            log.warn("[DemoDataSeeder] 写入演示商品失败：{}", e.getMessage());
        }
        try {
            seedCarouselIfEmpty();
        } catch (Exception e) {
            log.warn("[DemoDataSeeder] 写入演示轮播失败：{}", e.getMessage());
        }
    }

    /**
     * 把 classpath 下的演示图片复制到上传目录（已存在则跳过）。
     * 复制后它们即可通过 {@code GET /uploads/xxx.png} 访问。
     */
    private void copyDemoImagesToUploadDir() {
        try {
            File dir = new File(fileStorageService.getUploadDir());
            if (!dir.exists() && !dir.mkdirs()) {
                log.warn("[DemoDataSeeder] 上传目录创建失败，跳过图片复制：{}", dir.getPath());
                return;
            }
            int copied = 0;
            for (String name : DEMO_IMAGES) {
                File target = new File(dir, name);
                if (target.isFile() && target.length() > 0) {
                    continue; // 已存在，不覆盖
                }
                ClassPathResource resource = new ClassPathResource("demo-images/" + name);
                if (!resource.exists()) {
                    log.warn("[DemoDataSeeder] 演示图片缺失：demo-images/{}", name);
                    continue;
                }
                try (InputStream in = resource.getInputStream()) {
                    Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    copied++;
                }
            }
            if (copied > 0) {
                log.info("[DemoDataSeeder] 已复制 {} 张演示图片到上传目录：{}", copied, dir.getPath());
            }
        } catch (Exception e) {
            log.warn("[DemoDataSeeder] 复制演示图片失败：{}", e.getMessage());
        }
    }

    /** 商品表为空时写入演示商品 */
    private void seedProductsIfEmpty() {
        if (shangPinRepository.count() > 0) {
            log.info("[DemoDataSeeder] shangpin 已有数据，跳过演示商品写入");
            return;
        }
        List<ShangPin> list = new ArrayList<>();
        for (int i = 0; i < DEMO_PRODUCT_COUNT; i++) {
            list.add(buildDemoProduct(i));
        }
        shangPinRepository.saveAll(list);
        log.info("[DemoDataSeeder] 已写入 {} 条演示商品（归属用户 id={}）", list.size(), maiJiaId);
    }

    /** 轮播表为空时写入演示轮播 */
    private void seedCarouselIfEmpty() {
        if (lunBoTuRepository.count() > 0) {
            log.info("[DemoDataSeeder] lunbotu 已有数据，跳过演示轮播写入");
            return;
        }
        String[] titles = {"满级全干员账号", "顶级安全箱3*3", "黑鹰段位成品号"};
        List<LunBoTu> list = new ArrayList<>();
        for (int i = 0; i < titles.length; i++) {
            list.add(LunBoTu.builder()
                    .tuPianUrl(uploadUrl(i))
                    .tieTle(titles[i])
                    .shunXu(i + 1)
                    .zhuangTai(1)
                    .build());
        }
        lunBoTuRepository.saveAll(list);
        log.info("[DemoDataSeeder] 已写入 {} 条演示轮播", list.size());
    }

    private ShangPin buildDemoProduct(int index) {
        String accountType = index % 2 == 0 ? "纯币号" : "成品号";
        String server = index % 2 == 0 ? "QQ" : "微信";
        String rank = RANKS[index % RANKS.length];
        String safeBox = SAFE_BOXES[index % SAFE_BOXES.length];
        int pureCoinM = 20 + index * 8;
        int warehouseAssets = 50 + index * 5;
        String halfCoinRatio = (index == 0) ? "38" : "35";
        String title = String.format("%s丨%s丨%s", accountType, rank, safeBox);

        ShangPin sp = new ShangPin();
        sp.setMaiJiaId(maiJiaId);
        sp.setBiaoTi(title);
        sp.setMiaoShu("官方认证优质账号，人工审核，信息与描述一致，售后全程保障。");
        sp.setYouXiZhangHao("delta_demo_" + (index + 1));
        sp.setYouXiMiMa("demo123456");
        sp.setZhangHaoMiaoShu("演示账号，可放心租用。");

        // 日租金 = 纯币折算（与前端 mock 的算法保持一致）
        BigDecimal ratio = new BigDecimal(halfCoinRatio);
        BigDecimal preRent = BigDecimal.valueOf(pureCoinM * 100L)
                .divide(ratio, 2, RoundingMode.HALF_UP);
        sp.setRiZuJiaGe(preRent);
        sp.setZuiShaoZuJieTianShu(1);
        sp.setZhuangTai(1); // 已上架，立即可在商城看到
        sp.setZuJieCiShu(10 + index * 3);
        sp.setMetadata(buildMetadata(index, accountType, server, rank, safeBox,
                pureCoinM, warehouseAssets, halfCoinRatio, preRent, title));
        return sp;
    }

    /** 组装 metadata JSON：把前端详情页/卡片需要的字段都带上 */
    private String buildMetadata(int index, String accountType, String server, String rank,
                                 String safeBox, int pureCoinM, int warehouseAssets,
                                 String halfCoinRatio, BigDecimal preRent, String title) {
        String imageUrl = uploadUrl(index);
        Map<String, Object> meta = new LinkedHashMap<>();
        // 图片（后端托管：数据库只存 URL）
        meta.put("coverImageUrl", imageUrl);
        meta.put("tuPianUrl", imageUrl);
        meta.put("images", List.of(imageUrl));

        // 基础信息
        meta.put("mingCheng", title);
        meta.put("personalTag", title);
        meta.put("accountType", accountType);
        meta.put("server", server);
        meta.put("quFu", server);
        meta.put("settlementType", "哈夫币必须打完");
        meta.put("banRecord", "无封禁记录");
        meta.put("faceSelf", "是");

        // 资产
        meta.put("warehouseAssets", String.valueOf(warehouseAssets));
        meta.put("zongZiChan", String.valueOf(warehouseAssets));
        meta.put("pureCoinM", String.valueOf(pureCoinM));
        meta.put("halfCoinRatio", halfCoinRatio);

        // 等级
        meta.put("trainingLevel", String.valueOf(4 + index % 4));
        meta.put("rangeLevel", String.valueOf(4 + index % 4));
        meta.put("safeBox", safeBox);
        meta.put("rank", rank);
        meta.put("accountLevel", String.valueOf(40 + index * 3));

        // 装备
        meta.put("awmBullets", String.valueOf(100 + index * 50));
        meta.put("sixHead", String.valueOf(3 + index));
        meta.put("sixArmor", String.valueOf(2 + index));
        meta.put("secretKD", String.valueOf(2.0 + index * 0.3));

        // 皮肤
        meta.put("knifeSkins", KNIFE_SKINS[index % KNIFE_SKINS.length]);
        meta.put("operatorSkins", OPERATOR_SKINS[index % OPERATOR_SKINS.length]);
        meta.put("weaponSkins", WEAPON_SKINS[index % WEAPON_SKINS.length]);

        // 交易与结算
        meta.put("ownerBanned", "禁止恶意组队、禁止开挂");
        meta.put("ownerGifted", "赠送5个六头");
        meta.put("remark", "账号状态良好，无异常记录");
        meta.put("tradeStartHour", "08");
        meta.put("tradeEndHour", "22");
        meta.put("settlementRatio", "standard");
        meta.put("quickRatioValue", "");
        meta.put("preRent", preRent.toPlainString());
        meta.put("deposit", new BigDecimal(halfCoinRatio).multiply(BigDecimal.TEN).toPlainString());

        return gson.toJson(meta);
    }

    /**
     * 相对 URL：/uploads/demo-N.png。
     * 存相对路径而不是绝对地址，前端会按 API 源补全，端口/域名变化时无需改数据。
     */
    private String uploadUrl(int index) {
        return FileStorageService.URL_PREFIX + DEMO_IMAGES[index % DEMO_IMAGES.length];
    }
}

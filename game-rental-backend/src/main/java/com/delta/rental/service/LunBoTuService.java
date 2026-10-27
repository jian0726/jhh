package com.delta.rental.service;

import com.delta.rental.dto.LunBoTuRequest;
import com.delta.rental.entity.LunBoTu;
import com.delta.rental.exception.BusinessException;
import com.delta.rental.repository.LunBoTuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 轮播图服务。
 *
 * <p>图片本身由 {@link FileStorageService} 落盘托管，本服务只负责把图片的访问 URL
 * 写进/读出数据库。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LunBoTuService {

    private final LunBoTuRepository lunBoTuRepository;
    private final FileStorageService fileStorageService;

    /** 首页用：只返回启用中的轮播，按顺序号升序 */
    public List<LunBoTu> getActiveLunBoTu() {
        return lunBoTuRepository.findByZhuangTaiOrderByShunXuAsc(1);
    }

    /** 管理页用：返回全部轮播（含停用的） */
    public List<LunBoTu> getAllLunBoTu() {
        return lunBoTuRepository.findAllByOrderByShunXuAsc();
    }

    public LunBoTu getLunBoTuById(Long id) {
        return lunBoTuRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "轮播图不存在"));
    }

    @Transactional
    public LunBoTu createLunBoTu(LunBoTuRequest request) {
        String imageUrl = firstNonBlank(request.getTuPianUrl(), request.getImageUrl());
        if (imageUrl == null) {
            throw new BusinessException(400, "请先上传轮播图片");
        }
        LunBoTu entity = LunBoTu.builder()
                .tuPianUrl(imageUrl)
                .tieTle(firstNonBlank(request.getTieTle(), request.getTitle()))
                .lianJieUrl(request.getLianJieUrl())
                .shunXu(request.getShunXu() != null ? request.getShunXu() : 0)
                .zhuangTai(request.getZhuangTai() != null ? request.getZhuangTai() : 1)
                .build();
        return lunBoTuRepository.save(entity);
    }

    @Transactional
    public LunBoTu updateLunBoTu(Long id, LunBoTuRequest request) {
        LunBoTu entity = getLunBoTuById(id);

        String imageUrl = firstNonBlank(request.getTuPianUrl(), request.getImageUrl());
        if (imageUrl != null && !imageUrl.equals(entity.getTuPianUrl())) {
            // 换了图片：删掉旧文件，避免 uploads 目录无限膨胀（删不掉不影响本次更新）
            fileStorageService.deleteByUrl(entity.getTuPianUrl());
            entity.setTuPianUrl(imageUrl);
        }

        String title = firstNonBlank(request.getTieTle(), request.getTitle());
        if (title != null) {
            entity.setTieTle(title);
        }
        if (request.getLianJieUrl() != null) {
            entity.setLianJieUrl(request.getLianJieUrl());
        }
        if (request.getShunXu() != null) {
            entity.setShunXu(request.getShunXu());
        }
        if (request.getZhuangTai() != null) {
            entity.setZhuangTai(request.getZhuangTai());
        }
        return lunBoTuRepository.save(entity);
    }

    @Transactional
    public void deleteLunBoTu(Long id) {
        LunBoTu entity = getLunBoTuById(id);
        fileStorageService.deleteByUrl(entity.getTuPianUrl());
        lunBoTuRepository.delete(entity);
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }
}

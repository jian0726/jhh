package com.delta.rental.service;

import com.delta.rental.entity.ImHuiHua;
import com.delta.rental.entity.ImXiaoXi;
import com.delta.rental.exception.BusinessException;
import com.delta.rental.repository.ImHuiHuaRepository;
import com.delta.rental.repository.ImXiaoXiRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImService {

    private final ImHuiHuaRepository imHuiHuaRepository;
    private final ImXiaoXiRepository imXiaoXiRepository;

    public ImHuiHua createSession(Long userId1, Long userId2) {
        if (userId1.equals(userId2)) {
            throw new BusinessException(400, "不能与自己创建会话");
        }

        return imHuiHuaRepository.findByTwoUsers(userId1, userId2)
                .orElseGet(() -> {
                    String huiHuaHao = UUID.randomUUID().toString().replace("-", "").substring(0, 20);
                    ImHuiHua huiHua = ImHuiHua.builder()
                            .huiHuaHao(huiHuaHao)
                            .yongHuId1(userId1)
                            .yongHuId2(userId2)
                            .build();
                    return imHuiHuaRepository.save(huiHua);
                });
    }

    public Page<ImHuiHua> getSessions(Long userId, Pageable pageable) {
        return imHuiHuaRepository.findByUserId(userId, pageable);
    }

    public Page<ImXiaoXi> getMessages(String huiHuaHao, Long userId, Pageable pageable) {
        ImHuiHua huiHua = imHuiHuaRepository.findByHuiHuaHao(huiHuaHao)
                .orElseThrow(() -> new BusinessException(404, "会话不存在"));

        if (!huiHua.getYongHuId1().equals(userId) && !huiHua.getYongHuId2().equals(userId)) {
            throw new BusinessException(403, "无权访问该会话");
        }

        imXiaoXiRepository.markAsRead(huiHuaHao, userId);

        return imXiaoXiRepository.findByHuiHuaHaoOrderByFaSongShiJianAsc(huiHuaHao, pageable);
    }

    @Transactional
    public ImXiaoXi sendMessage(String huiHuaHao, Long faSongRenId, Long shouDaoRenId, String neiRong) {
        ImHuiHua huiHua = imHuiHuaRepository.findByHuiHuaHao(huiHuaHao)
                .orElseThrow(() -> new BusinessException(404, "会话不存在"));

        if (!huiHua.getYongHuId1().equals(faSongRenId) || !huiHua.getYongHuId2().equals(shouDaoRenId)) {
            if (!huiHua.getYongHuId2().equals(faSongRenId) || !huiHua.getYongHuId1().equals(shouDaoRenId)) {
                throw new BusinessException(403, "无权发送消息");
            }
        }

        ImXiaoXi xiaoXi = ImXiaoXi.builder()
                .huiHuaHao(huiHuaHao)
                .faSongRenId(faSongRenId)
                .shouDaoRenId(shouDaoRenId)
                .neiRong(neiRong)
                .xiaoXiLeiXing("TEXT")
                .shiFouYiDu(0)
                .build();

        ImXiaoXi saved = imXiaoXiRepository.save(xiaoXi);

        huiHua.setZuiHouXiaoXiShiJian(LocalDateTime.now());
        imHuiHuaRepository.save(huiHua);

        return saved;
    }

    public Long getUnreadCount(String huiHuaHao, Long userId) {
        return imXiaoXiRepository.countUnread(huiHuaHao, userId);
    }

    public Page<ImHuiHua> getAllSessions(Pageable pageable) {
        return imHuiHuaRepository.findAllByOrderByZuiHouXiaoXiShiJianDesc(pageable);
    }

    @Transactional
    public void deleteSession(String huiHuaHao) {
        ImHuiHua huiHua = imHuiHuaRepository.findByHuiHuaHao(huiHuaHao)
                .orElseThrow(() -> new BusinessException(404, "会话不存在"));

        imXiaoXiRepository.deleteByHuiHuaHao(huiHuaHao);
        imHuiHuaRepository.delete(huiHua);
    }

    public Page<ImXiaoXi> searchMessages(String keyword, Pageable pageable) {
        return imXiaoXiRepository.searchByNeiRong(keyword, pageable);
    }

    public java.util.Map<String, Object> getStatistics() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();

        long totalSessions = imHuiHuaRepository.count();
        long totalMessages = imXiaoXiRepository.count();
        long todayMessages = imXiaoXiRepository.countToday();
        long activeUsers = imHuiHuaRepository.countDistinctUsers();

        stats.put("totalSessions", totalSessions);
        stats.put("totalMessages", totalMessages);
        stats.put("todayMessages", todayMessages);
        stats.put("activeUsers", activeUsers);

        return stats;
    }
}
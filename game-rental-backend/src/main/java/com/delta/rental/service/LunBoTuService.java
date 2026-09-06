package com.delta.rental.service;

import com.delta.rental.entity.LunBoTu;
import com.delta.rental.repository.LunBoTuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LunBoTuService {

    private final LunBoTuRepository lunBoTuRepository;

    public List<LunBoTu> getActiveLunBoTu() {
        return lunBoTuRepository.findByZhuangTaiOrderByShunXuAsc(1);
    }
}
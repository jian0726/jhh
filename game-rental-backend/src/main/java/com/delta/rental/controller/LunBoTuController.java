package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.entity.LunBoTu;
import com.delta.rental.service.LunBoTuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LunBoTuController {

    private final LunBoTuService lunBoTuService;

    @GetMapping("/lunbotu")
    public ApiResponse<List<LunBoTu>> getLunBoTu() {
        List<LunBoTu> list = lunBoTuService.getActiveLunBoTu();
        return ApiResponse.success(list);
    }
}
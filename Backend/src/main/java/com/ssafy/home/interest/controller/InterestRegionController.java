package com.ssafy.home.interest.controller;

import com.ssafy.home.common.response.ApiResponse;
import com.ssafy.home.interest.dto.InterestRegionRequest;
import com.ssafy.home.interest.dto.InterestRegionResponse;
import com.ssafy.home.interest.service.InterestRegionService;
import com.ssafy.home.member.auth.CurrentMemberId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interest-regions")
public class InterestRegionController {

    private final InterestRegionService service;

    public InterestRegionController(InterestRegionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InterestRegionResponse>>> myRegions(@CurrentMemberId Long memberId) {
        return ResponseEntity.ok(ApiResponse.ok(service.findMyRegions(memberId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InterestRegionResponse>> add(
            @RequestBody InterestRegionRequest requestBody,
            @CurrentMemberId Long memberId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("created", service.addMyRegion(memberId, requestBody)));
    }

    @DeleteMapping("/{interestRegionId}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> delete(
            @PathVariable Long interestRegionId,
            @CurrentMemberId Long memberId
    ) {
        service.deleteMyRegion(memberId, interestRegionId);
        return ResponseEntity.ok(ApiResponse.ok("deleted", Map.of("deleted", true)));
    }
}

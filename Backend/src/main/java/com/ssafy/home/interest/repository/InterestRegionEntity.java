package com.ssafy.home.interest.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "interest_regions")
public class InterestRegionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interest_region_id")
    private Long interestRegionId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private RegionEntity region;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected InterestRegionEntity() {
    }

    public InterestRegionEntity(Long memberId, RegionEntity region) {
        this.memberId = memberId;
        this.region = region;
    }

    public Long getInterestRegionId() {
        return interestRegionId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public RegionEntity getRegion() {
        return region;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

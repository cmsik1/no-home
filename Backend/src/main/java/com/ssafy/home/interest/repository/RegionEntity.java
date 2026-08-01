package com.ssafy.home.interest.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "regions", uniqueConstraints = @UniqueConstraint(name = "uq_regions_lawd_umd", columnNames = {"lawd_cd", "umd_nm"}))
public class RegionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "region_id")
    private Long regionId;

    @Column(name = "lawd_cd", nullable = false, length = 5)
    private String lawdCd;

    @Column(name = "legal_dong_code", length = 10)
    private String legalDongCode;

    @Column(nullable = false)
    private String sido;

    @Column(nullable = false)
    private String sigungu;

    @Column(name = "umd_nm", nullable = false)
    private String umdNm;

    protected RegionEntity() {
    }

    public RegionEntity(String lawdCd, String legalDongCode, String sido, String sigungu, String umdNm) {
        this.lawdCd = lawdCd;
        this.legalDongCode = legalDongCode;
        this.sido = sido;
        this.sigungu = sigungu;
        this.umdNm = umdNm;
    }

    public Long getRegionId() {
        return regionId;
    }

    public String getLawdCd() {
        return lawdCd;
    }

    public String getLegalDongCode() {
        return legalDongCode;
    }

    public String getSido() {
        return sido;
    }

    public String getSigungu() {
        return sigungu;
    }

    public String getUmdNm() {
        return umdNm;
    }

    public void update(String legalDongCode, String sido, String sigungu) {
        this.legalDongCode = legalDongCode;
        this.sido = sido;
        this.sigungu = sigungu;
    }
}

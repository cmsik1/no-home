package com.ssafy.home.publicdata.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.ssafy.home.common.persistence.JpaRows.intValue;
import static com.ssafy.home.common.persistence.JpaRows.longValue;
import static com.ssafy.home.common.persistence.JpaRows.stringValue;

@Repository
public class JpaPublicDataImportAdapter implements PublicDataImportPersistencePort {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Long> selectSuccessBatchId(String sourceApi, String lawdCd, String dealYmd, String houseType, String dealType) {
        List<?> rows = entityManager.createNativeQuery("""
                        SELECT import_batch_id
                        FROM public_data_import_batches
                        WHERE source_api = :sourceApi
                          AND lawd_cd = :lawdCd
                          AND deal_ymd = :dealYmd
                          AND house_type = :houseType
                          AND deal_type = :dealType
                          AND status = 'success'
                          AND total_count <= imported_count + skipped_count
                        """)
                .setParameter("sourceApi", sourceApi)
                .setParameter("lawdCd", lawdCd)
                .setParameter("dealYmd", dealYmd)
                .setParameter("houseType", houseType)
                .setParameter("dealType", dealType)
                .getResultList();
        return rows.stream().findFirst().map(value -> ((Number) value).longValue());
    }

    @Override
    public void upsertRequestedBatch(String sourceApi, String lawdCd, String dealYmd, String houseType, String dealType) {
        entityManager.createNativeQuery("""
                        INSERT INTO public_data_import_batches (
                            source_api, lawd_cd, deal_ymd, house_type, deal_type,
                            status, total_count, imported_count, skipped_count, error_message,
                            requested_at, completed_at
                        )
                        VALUES (
                            :sourceApi, :lawdCd, :dealYmd, :houseType, :dealType,
                            'requested', 0, 0, 0, NULL, CURRENT_TIMESTAMP, NULL
                        )
                        ON CONFLICT (source_api, lawd_cd, deal_ymd, house_type, deal_type) DO UPDATE SET
                            status = 'requested',
                            total_count = 0,
                            imported_count = 0,
                            skipped_count = 0,
                            error_message = NULL,
                            requested_at = CURRENT_TIMESTAMP,
                            completed_at = NULL,
                            updated_at = CURRENT_TIMESTAMP
                        """)
                .setParameter("sourceApi", sourceApi)
                .setParameter("lawdCd", lawdCd)
                .setParameter("dealYmd", dealYmd)
                .setParameter("houseType", houseType)
                .setParameter("dealType", dealType)
                .executeUpdate();
    }

    @Override
    public void updateBatchSuccess(String sourceApi, String lawdCd, String dealYmd, String houseType, String dealType,
                                   int totalCount, int importedCount, int skippedCount) {
        entityManager.createNativeQuery("""
                        UPDATE public_data_import_batches
                        SET status = 'success',
                            total_count = :totalCount,
                            imported_count = :importedCount,
                            skipped_count = :skippedCount,
                            error_message = NULL,
                            completed_at = CURRENT_TIMESTAMP,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE source_api = :sourceApi
                          AND lawd_cd = :lawdCd
                          AND deal_ymd = :dealYmd
                          AND house_type = :houseType
                          AND deal_type = :dealType
                        """)
                .setParameter("sourceApi", sourceApi)
                .setParameter("lawdCd", lawdCd)
                .setParameter("dealYmd", dealYmd)
                .setParameter("houseType", houseType)
                .setParameter("dealType", dealType)
                .setParameter("totalCount", totalCount)
                .setParameter("importedCount", importedCount)
                .setParameter("skippedCount", skippedCount)
                .executeUpdate();
    }

    @Override
    public void updateBatchFailed(String sourceApi, String lawdCd, String dealYmd, String houseType, String dealType, String errorMessage) {
        entityManager.createNativeQuery("""
                        UPDATE public_data_import_batches
                        SET status = 'failed',
                            error_message = :errorMessage,
                            completed_at = CURRENT_TIMESTAMP,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE source_api = :sourceApi
                          AND lawd_cd = :lawdCd
                          AND deal_ymd = :dealYmd
                          AND house_type = :houseType
                          AND deal_type = :dealType
                        """)
                .setParameter("sourceApi", sourceApi)
                .setParameter("lawdCd", lawdCd)
                .setParameter("dealYmd", dealYmd)
                .setParameter("houseType", houseType)
                .setParameter("dealType", dealType)
                .setParameter("errorMessage", errorMessage)
                .executeUpdate();
    }

    @Override
    public void upsertRegion(String lawdCd, String sido, String sigungu, String umdNm) {
        upsertRegions(List.of(new RegionIdentity(lawdCd, sido, sigungu, umdNm)));
    }

    @Override
    public Optional<Long> selectRegionId(String lawdCd, String umdNm) {
        List<?> rows = entityManager.createNativeQuery("""
                        SELECT region_id
                        FROM regions
                        WHERE lawd_cd = :lawdCd
                          AND umd_nm = :umdNm
                        """)
                .setParameter("lawdCd", lawdCd)
                .setParameter("umdNm", umdNm)
                .getResultList();
        return rows.stream().findFirst().map(value -> ((Number) value).longValue());
    }

    @Override
    public void upsertHouse(HouseUpsertCommand command) {
        upsertHouses(List.of(command));
    }

    @Override
    public Optional<Long> selectHouseId(HouseUpsertCommand command) {
        return selectHouseIds(List.of(command)).stream().findFirst().map(HouseIdMapping::houseId);
    }

    @Override
    public int insertHouseDealIfAbsent(HouseDealInsertCommand command) {
        return insertHouseDealsIfAbsent(List.of(command));
    }

    @Override
    public void upsertRegions(List<RegionIdentity> regions) {
        for (RegionIdentity region : regions) {
            entityManager.createNativeQuery("""
                            INSERT INTO regions (lawd_cd, sido, sigungu, umd_nm)
                            VALUES (:lawdCd, :sido, :sigungu, :umdNm)
                            ON CONFLICT (lawd_cd, umd_nm) DO UPDATE SET updated_at = CURRENT_TIMESTAMP
                            """)
                    .setParameter("lawdCd", region.lawdCd())
                    .setParameter("sido", region.sido())
                    .setParameter("sigungu", region.sigungu())
                    .setParameter("umdNm", region.umdNm())
                    .executeUpdate();
        }
    }

    @Override
    public List<RegionIdMapping> selectRegionIds(List<RegionIdentity> regions) {
        List<RegionIdMapping> mappings = new ArrayList<>();
        for (RegionIdentity region : regions) {
            selectRegionId(region.lawdCd(), region.umdNm())
                    .map(regionId -> new RegionIdMapping(regionId, region.lawdCd(), region.umdNm()))
                    .ifPresent(mappings::add);
        }
        return mappings;
    }

    @Override
    public void upsertHouses(List<HouseUpsertCommand> commands) {
        for (HouseUpsertCommand command : commands) {
            entityManager.createNativeQuery("""
                            INSERT INTO houses (region_id, sgg_cd, umd_nm, jibun, apt_nm, build_year)
                            VALUES (:regionId, :sggCd, :umdNm, :jibun, :aptNm, :buildYear)
                            ON CONFLICT (sgg_cd, umd_nm, jibun, apt_nm, build_year) DO UPDATE SET
                                region_id = EXCLUDED.region_id,
                                updated_at = CURRENT_TIMESTAMP
                            """)
                    .setParameter("regionId", command.regionId())
                    .setParameter("sggCd", command.sggCd())
                    .setParameter("umdNm", command.umdNm())
                    .setParameter("jibun", command.jibun())
                    .setParameter("aptNm", command.aptNm())
                    .setParameter("buildYear", command.buildYear())
                    .executeUpdate();
        }
    }

    @Override
    public List<HouseIdMapping> selectHouseIds(List<HouseUpsertCommand> commands) {
        List<HouseIdMapping> mappings = new ArrayList<>();
        for (HouseUpsertCommand command : commands) {
            List<?> rows = entityManager.createNativeQuery("""
                            SELECT house_id, sgg_cd, umd_nm, jibun, apt_nm, build_year
                            FROM houses
                            WHERE sgg_cd = :sggCd
                              AND umd_nm = :umdNm
                              AND jibun = :jibun
                              AND apt_nm = :aptNm
                              AND ((build_year IS NULL AND CAST(:buildYear AS integer) IS NULL)
                                   OR build_year = CAST(:buildYear AS integer))
                            """)
                    .setParameter("sggCd", command.sggCd())
                    .setParameter("umdNm", command.umdNm())
                    .setParameter("jibun", command.jibun())
                    .setParameter("aptNm", command.aptNm())
                    .setParameter("buildYear", command.buildYear())
                    .getResultList();
            rows.stream().findFirst()
                    .map(row -> toHouseIdMapping((Object[]) row))
                    .ifPresent(mappings::add);
        }
        return mappings;
    }

    @Override
    public int insertHouseDealsIfAbsent(List<HouseDealInsertCommand> commands) {
        int inserted = 0;
        for (HouseDealInsertCommand command : commands) {
            Query query = entityManager.createNativeQuery("""
                            INSERT INTO house_deals (
                                house_id, source_api, lawd_cd, deal_ymd, house_type, deal_type,
                                deal_year, deal_month, deal_day, deal_date, deal_amount, deal_amount_manwon,
                                rent_type, deposit, deposit_manwon, monthly_rent, monthly_rent_manwon,
                                exclu_use_ar, floor, apt_dong, buyer_gbn, sler_gbn, dealing_gbn,
                                estate_agent_sgg_nm, cdeal_type, cdeal_day, rgst_date, land_leasehold_gbn,
                                contract_term, contract_type, use_rr_right, pre_deposit, pre_deposit_manwon,
                                pre_monthly_rent, pre_monthly_rent_manwon, roadnm, apt_seq,
                                api_row_hash, raw_response
                            )
                            VALUES (
                                :houseId, :sourceApi, :lawdCd, :dealYmd, 'apartment', :dealType,
                                :dealYear, :dealMonth, :dealDay, :dealDate, :dealAmount, :dealAmountManwon,
                                :rentType, :deposit, :depositManwon, :monthlyRent, :monthlyRentManwon,
                                :excluUseAr, :floor, :aptDong, :buyerGbn, :slerGbn, :dealingGbn,
                                :estateAgentSggNm, :cdealType, :cdealDay, :rgstDate, :landLeaseholdGbn,
                                :contractTerm, :contractType, :useRRRight, :preDeposit, :preDepositManwon,
                                :preMonthlyRent, :preMonthlyRentManwon, :roadnm, :aptSeq,
                                :apiRowHash, CAST(:rawResponse AS jsonb)
                            )
                            ON CONFLICT (api_row_hash) DO NOTHING
                            """);
            bindDeal(query, command);
            inserted += query.executeUpdate();
        }
        return inserted;
    }

    private static void bindDeal(Query query, HouseDealInsertCommand command) {
        query.setParameter("houseId", command.houseId());
        query.setParameter("sourceApi", command.sourceApi());
        query.setParameter("lawdCd", command.lawdCd());
        query.setParameter("dealYmd", command.dealYmd());
        query.setParameter("dealType", command.dealType());
        query.setParameter("dealYear", command.dealYear());
        query.setParameter("dealMonth", command.dealMonth());
        query.setParameter("dealDay", command.dealDay());
        query.setParameter("dealDate", command.dealDate());
        query.setParameter("dealAmount", command.dealAmount());
        query.setParameter("dealAmountManwon", command.dealAmountManwon());
        query.setParameter("rentType", command.rentType());
        query.setParameter("deposit", command.deposit());
        query.setParameter("depositManwon", command.depositManwon());
        query.setParameter("monthlyRent", command.monthlyRent());
        query.setParameter("monthlyRentManwon", command.monthlyRentManwon());
        query.setParameter("excluUseAr", command.excluUseAr());
        query.setParameter("floor", command.floor());
        query.setParameter("aptDong", command.aptDong());
        query.setParameter("buyerGbn", command.buyerGbn());
        query.setParameter("slerGbn", command.slerGbn());
        query.setParameter("dealingGbn", command.dealingGbn());
        query.setParameter("estateAgentSggNm", command.estateAgentSggNm());
        query.setParameter("cdealType", command.cdealType());
        query.setParameter("cdealDay", command.cdealDay());
        query.setParameter("rgstDate", command.rgstDate());
        query.setParameter("landLeaseholdGbn", command.landLeaseholdGbn());
        query.setParameter("contractTerm", command.contractTerm());
        query.setParameter("contractType", command.contractType());
        query.setParameter("useRRRight", command.useRRRight());
        query.setParameter("preDeposit", command.preDeposit());
        query.setParameter("preDepositManwon", command.preDepositManwon());
        query.setParameter("preMonthlyRent", command.preMonthlyRent());
        query.setParameter("preMonthlyRentManwon", command.preMonthlyRentManwon());
        query.setParameter("roadnm", command.roadnm());
        query.setParameter("aptSeq", command.aptSeq());
        query.setParameter("apiRowHash", command.apiRowHash());
        query.setParameter("rawResponse", command.rawResponse());
    }

    private static HouseIdMapping toHouseIdMapping(Object[] row) {
        return new HouseIdMapping(longValue(row[0]), stringValue(row[1]), stringValue(row[2]),
                stringValue(row[3]), stringValue(row[4]), intValue(row[5]));
    }
}

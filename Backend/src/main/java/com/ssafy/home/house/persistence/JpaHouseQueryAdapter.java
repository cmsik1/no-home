package com.ssafy.home.house.persistence;

import com.ssafy.home.house.dto.HouseDealPriceRangeResponse;
import com.ssafy.home.house.dto.HouseDealResponse;
import com.ssafy.home.house.dto.HouseResponse;
import com.ssafy.home.house.dto.HouseSearchCondition;
import com.ssafy.home.house.dto.HouseSearchResultResponse;
import com.ssafy.home.house.dto.ImportBatchResponse;
import com.ssafy.home.house.dto.RegionResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.ssafy.home.common.persistence.JpaRows.intValue;
/**
 * {@link HousePersistencePort}를 JPA {@link EntityManager}와 native SQL로 구현한다.
 * query 결과의 {@code Object[]} row는 {@link HouseRowMappers}에서 API용 DTO로 변환된다.
 */
@Repository
public class JpaHouseQueryAdapter implements HousePersistencePort {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<RegionResponse> selectRegionsByLawdCd(String lawdCd) {
        return rows(entityManager.createNativeQuery("""
                        SELECT region_id, lawd_cd, legal_dong_code, sido, sigungu, umd_nm, lat, lng
                        FROM regions
                        WHERE lawd_cd = :lawdCd
                        ORDER BY umd_nm
                        """)
                .setParameter("lawdCd", lawdCd))
                .stream()
                .map(HouseRowMappers::toRegion)
                .toList();
    }

    @Override
    public List<HouseResponse> selectHousesByAptName(String aptName) {
        return rows(entityManager.createNativeQuery("""
                        SELECT house_id, region_id, sgg_cd, umd_nm, jibun, apt_nm, build_year, lat, lng
                        FROM houses
                        WHERE apt_nm LIKE CONCAT('%', :aptName, '%')
                        ORDER BY apt_nm, umd_nm, jibun
                        """)
                .setParameter("aptName", aptName))
                .stream()
                .map(HouseRowMappers::toHouse)
                .toList();
    }

    @Override
    public List<HouseDealResponse> selectHouseDeals(String lawdCd, String dealYmd) {
        return rows(entityManager.createNativeQuery("""
                        SELECT
                            hd.deal_id, hd.house_id, h.apt_nm, h.umd_nm, h.jibun, hd.lawd_cd, hd.deal_ymd,
                            hd.deal_type, hd.rent_type, hd.deal_date, hd.deal_amount, hd.deal_amount_manwon,
                            hd.deposit, hd.deposit_manwon, hd.monthly_rent, hd.monthly_rent_manwon,
                            hd.exclu_use_ar, hd.floor
                        FROM house_deals hd
                        INNER JOIN houses h ON h.house_id = hd.house_id
                        WHERE hd.lawd_cd = :lawdCd
                          AND hd.deal_ymd = :dealYmd
                        ORDER BY hd.deal_date DESC, h.apt_nm, hd.deal_id DESC
                        """)
                .setParameter("lawdCd", lawdCd)
                .setParameter("dealYmd", dealYmd))
                .stream()
                .map(HouseRowMappers::toDeal)
                .toList();
    }

    @Override
    public List<HouseSearchResultResponse> searchHouseDeals(HouseSearchCondition condition) {
        HouseSearchNativeSql.SearchSql sql = HouseSearchNativeSql.from(condition, true);
        Query query = bind(entityManager.createNativeQuery("""
                        SELECT
                            hd.deal_id, hd.house_id, h.apt_nm, r.sido, r.sigungu, h.umd_nm, h.jibun, h.build_year,
                            hd.lawd_cd, hd.deal_ymd, hd.deal_type, hd.rent_type, hd.deal_date, hd.deal_amount,
                            hd.deal_amount_manwon, hd.deposit, hd.deposit_manwon, hd.monthly_rent,
                            hd.monthly_rent_manwon, hd.exclu_use_ar, hd.floor, hd.contract_term, hd.contract_type,
                            hd.use_rr_right, hd.pre_deposit, hd.pre_deposit_manwon, hd.pre_monthly_rent,
                            hd.pre_monthly_rent_manwon, hd.roadnm, hd.apt_seq, h.lat, h.lng,
                            hd.api_row_hash, hd.api_row_hash
                        """ + sql.sql()), sql.params())
                .setParameter("size", condition.size())
                .setParameter("offset", condition.offset());
        return rows(query).stream()
                .map(HouseRowMappers::toSearchResult)
                .toList();
    }

    @Override
    public long countHouseDeals(HouseSearchCondition condition) {
        HouseSearchNativeSql.SearchSql sql = HouseSearchNativeSql.from(condition, false);
        Object count = bind(entityManager.createNativeQuery("SELECT COUNT(*) " + sql.sql()), sql.params()).getSingleResult();
        return ((Number) count).longValue();
    }

    @Override
    public HouseDealPriceRangeResponse selectHouseDealPriceRange(HouseSearchCondition condition) {
        HouseSearchNativeSql.SearchSql sql = HouseSearchNativeSql.from(condition, false);
        Object[] row = (Object[]) bind(entityManager.createNativeQuery("""
                SELECT
                    MIN(hd.deal_amount_manwon), MAX(hd.deal_amount_manwon),
                    MIN(hd.deposit_manwon), MAX(hd.deposit_manwon),
                    MIN(hd.monthly_rent_manwon), MAX(hd.monthly_rent_manwon)
                """ + sql.sql()), sql.params()).getSingleResult();
        return new HouseDealPriceRangeResponse(
                intValue(row[0]), intValue(row[1]), intValue(row[2]),
                intValue(row[3]), intValue(row[4]), intValue(row[5])
        );
    }

    @Override
    public Optional<ImportBatchResponse> selectImportBatch(
            String sourceApi,
            String lawdCd,
            String dealYmd,
            String houseType,
            String dealType
    ) {
        List<Object[]> rows = rows(entityManager.createNativeQuery("""
                        SELECT import_batch_id, source_api, lawd_cd, deal_ymd, house_type, deal_type, status,
                               total_count, imported_count, skipped_count, error_message, requested_at, completed_at
                        FROM public_data_import_batches
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
                .setParameter("dealType", dealType));
        return rows.stream().findFirst().map(HouseRowMappers::toImportBatch);
    }

    private static Query bind(Query query, Map<String, Object> params) {
        params.forEach(query::setParameter);
        return query;
    }

    @SuppressWarnings("unchecked")
    private static List<Object[]> rows(Query query) {
        return (List<Object[]>) (List<?>) query.getResultList();
    }
}

package com.ssafy.home.publicdata.persistence;

import java.util.List;
import java.util.Optional;

public interface PublicDataImportPersistencePort {
    Optional<Long> selectSuccessBatchId(String sourceApi, String lawdCd, String dealYmd, String houseType, String dealType);
    void upsertRequestedBatch(String sourceApi, String lawdCd, String dealYmd, String houseType, String dealType);
    void updateBatchSuccess(String sourceApi, String lawdCd, String dealYmd, String houseType, String dealType,
                            int totalCount, int importedCount, int skippedCount);
    void updateBatchFailed(String sourceApi, String lawdCd, String dealYmd, String houseType, String dealType,
                           String errorMessage);
    void upsertRegion(String lawdCd, String sido, String sigungu, String umdNm);
    Optional<Long> selectRegionId(String lawdCd, String umdNm);
    void upsertHouse(HouseUpsertCommand command);
    Optional<Long> selectHouseId(HouseUpsertCommand command);
    int insertHouseDealIfAbsent(HouseDealInsertCommand command);
    void upsertRegions(List<RegionIdentity> regions);
    List<RegionIdMapping> selectRegionIds(List<RegionIdentity> regions);
    void upsertHouses(List<HouseUpsertCommand> commands);
    List<HouseIdMapping> selectHouseIds(List<HouseUpsertCommand> commands);
    int insertHouseDealsIfAbsent(List<HouseDealInsertCommand> commands);
}

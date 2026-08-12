package com.ssafy.home.house.persistence;

import com.ssafy.home.house.dto.HouseDealResponse;
import com.ssafy.home.house.dto.HouseResponse;
import com.ssafy.home.house.dto.HouseSearchResultResponse;
import com.ssafy.home.house.dto.ImportBatchResponse;
import com.ssafy.home.house.dto.RegionResponse;

import static com.ssafy.home.common.persistence.JpaRows.dateTimeValue;
import static com.ssafy.home.common.persistence.JpaRows.dateValue;
import static com.ssafy.home.common.persistence.JpaRows.decimalValue;
import static com.ssafy.home.common.persistence.JpaRows.intValue;
import static com.ssafy.home.common.persistence.JpaRows.longValue;
import static com.ssafy.home.common.persistence.JpaRows.stringValue;

/**
 * native query의 열 순서를 주택·거래·검색 응답 record에 매핑한다.
 * SQL projection이 바뀌면 이 변환과 DTO를 함께 검토해야 한다.
 */
final class HouseRowMappers {

    private HouseRowMappers() {
    }

    static RegionResponse toRegion(Object[] row) {
        return new RegionResponse(longValue(row[0]), stringValue(row[1]), stringValue(row[2]), stringValue(row[3]),
                stringValue(row[4]), stringValue(row[5]), decimalValue(row[6]), decimalValue(row[7]));
    }

    static HouseResponse toHouse(Object[] row) {
        return new HouseResponse(longValue(row[0]), longValue(row[1]), stringValue(row[2]), stringValue(row[3]),
                stringValue(row[4]), stringValue(row[5]), intValue(row[6]), decimalValue(row[7]), decimalValue(row[8]));
    }

    static HouseDealResponse toDeal(Object[] row) {
        return new HouseDealResponse(longValue(row[0]), longValue(row[1]), stringValue(row[2]), stringValue(row[3]),
                stringValue(row[4]), stringValue(row[5]), stringValue(row[6]), stringValue(row[7]), stringValue(row[8]),
                dateValue(row[9]), stringValue(row[10]), intValue(row[11]), stringValue(row[12]), intValue(row[13]),
                stringValue(row[14]), intValue(row[15]), decimalValue(row[16]), intValue(row[17]));
    }

    static HouseSearchResultResponse toSearchResult(Object[] row) {
        return new HouseSearchResultResponse(longValue(row[0]), longValue(row[1]), stringValue(row[2]), stringValue(row[3]),
                stringValue(row[4]), stringValue(row[5]), stringValue(row[6]), intValue(row[7]), stringValue(row[8]),
                stringValue(row[9]), stringValue(row[10]), stringValue(row[11]), dateValue(row[12]), stringValue(row[13]),
                intValue(row[14]), stringValue(row[15]), intValue(row[16]), stringValue(row[17]), intValue(row[18]),
                decimalValue(row[19]), intValue(row[20]), stringValue(row[21]), stringValue(row[22]), stringValue(row[23]),
                stringValue(row[24]), intValue(row[25]), stringValue(row[26]), intValue(row[27]), stringValue(row[28]),
                stringValue(row[29]), decimalValue(row[30]), decimalValue(row[31]), stringValue(row[32]), stringValue(row[33]));
    }

    static ImportBatchResponse toImportBatch(Object[] row) {
        return new ImportBatchResponse(longValue(row[0]), stringValue(row[1]), stringValue(row[2]), stringValue(row[3]),
                stringValue(row[4]), stringValue(row[5]), stringValue(row[6]), intValue(row[7]), intValue(row[8]),
                intValue(row[9]), stringValue(row[10]), dateTimeValue(row[11]), dateTimeValue(row[12]));
    }
}

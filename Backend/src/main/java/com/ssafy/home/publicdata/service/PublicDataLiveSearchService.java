package com.ssafy.home.publicdata.service;

import com.ssafy.home.common.region.SeoulLawdCodeResolver;
import com.ssafy.home.house.dto.AutoImportRangeResponse;
import com.ssafy.home.house.dto.HouseDealPriceRangeResponse;
import com.ssafy.home.house.dto.HouseSearchCondition;
import com.ssafy.home.house.dto.HouseSearchPageResponse;
import com.ssafy.home.house.dto.HouseSearchResultResponse;
import com.ssafy.home.publicdata.dto.AptRentApiItem;
import com.ssafy.home.publicdata.dto.AptRentApiResponse;
import com.ssafy.home.publicdata.dto.AptTradeApiItem;
import com.ssafy.home.publicdata.dto.AptTradeApiResponse;
import com.ssafy.home.publicdata.persistence.HouseDealInsertCommand;
import com.ssafy.home.publicdata.persistence.HouseUpsertCommand;
import com.ssafy.home.publicdata.service.PublicDataBatchPersistService.PersistRequest;
import com.ssafy.home.publicdata.service.PublicDataBatchPersistService.PersistRow;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * DB coverage가 없는 검색에서 공공데이터 row를 즉시 API 응답 DTO로 바꾸는 service다.
 * 사용자는 적재 완료를 기다리지 않고 결과를 받고, 같은 row는 background batch로 DB에 저장된다.
 */
@Service
public class PublicDataLiveSearchService {

    private final PublicDataLivePageFetcher pageFetcher;
    private final AptTradeImportCommandFactory tradeCommandFactory;
    private final AptRentImportCommandFactory rentCommandFactory;
    private final SeoulLawdCodeResolver seoulLawdCodeResolver;
    private final PublicDataBatchPersistService persistService;

    public PublicDataLiveSearchService(
            PublicDataLivePageFetcher pageFetcher,
            AptTradeImportCommandFactory tradeCommandFactory,
            AptRentImportCommandFactory rentCommandFactory,
            SeoulLawdCodeResolver seoulLawdCodeResolver,
            PublicDataBatchPersistService persistService
    ) {
        this.pageFetcher = pageFetcher;
        this.tradeCommandFactory = tradeCommandFactory;
        this.rentCommandFactory = rentCommandFactory;
        this.seoulLawdCodeResolver = seoulLawdCodeResolver;
        this.persistService = persistService;
    }

    public HouseSearchPageResponse search(List<String> lawdCds, List<String> dealYmds, HouseSearchCondition condition) {
        List<LiveRow> rows = fetchRows(lawdCds, dealYmds, condition.dealMode());
        return LiveHouseSearchResultProcessor.process(
                rows.stream().map(LiveRow::response).toList(),
                rows.stream().map(LiveRow::range).toList(),
                condition
        );
    }

    public HouseDealPriceRangeResponse priceRange(List<String> lawdCds, List<String> dealYmds, HouseSearchCondition condition) {
        List<HouseSearchResultResponse> items = fetchRows(lawdCds, dealYmds, condition.dealMode()).stream()
                .map(LiveRow::response)
                .toList();
        return LiveHouseSearchResultProcessor.priceRange(items, condition);
    }

    /** 법정동·거래월 조합을 순회하며 거래 유형에 필요한 매매/전월세 API만 호출해 한 목록으로 합친다. */
    private List<LiveRow> fetchRows(List<String> lawdCds, List<String> dealYmds, String dealMode) {
        List<LiveRow> rows = new ArrayList<>();
        for (String lawdCd : lawdCds) {
            String sigungu = seoulLawdCodeResolver.sigunguName(lawdCd).orElse("");
            for (String dealYmd : dealYmds) {
                if (shouldFetchSale(dealMode)) {
                    rows.addAll(fetchSale(lawdCd, sigungu, dealYmd));
                }
                if (shouldFetchRent(dealMode)) {
                    rows.addAll(fetchRent(lawdCd, sigungu, dealYmd));
                }
            }
        }
        return rows;
    }

    private List<LiveRow> fetchSale(String lawdCd, String sigungu, String dealYmd) {
        AptTradeApiResponse response = pageFetcher.fetchTrades(lawdCd, dealYmd);
        List<PersistRow> persistRows = new ArrayList<>();
        List<LiveRow> liveRows = new ArrayList<>();
        for (AptTradeApiItem item : response.items()) {
            HouseUpsertCommand houseCommand = tradeCommandFactory.toHouseCommand(lawdCd, null, item);
            HouseDealInsertCommand dealCommand = tradeCommandFactory.toDealCommand(lawdCd, dealYmd, null, item);
            HouseSearchResultResponse result = toResponse(lawdCd, dealYmd, sigungu, houseCommand, dealCommand);
            persistRows.add(new PersistRow(lawdCd, sigungu, item.umdNm(), houseCommand, dealCommand));
            liveRows.add(new LiveRow(result, new AutoImportRangeResponse(lawdCd, dealYmd, "live",
                    AptTradeImportCommandFactory.SOURCE_API + " live response")));
        }
        persistService.persistAsync(new PersistRequest(AptTradeImportCommandFactory.SOURCE_API, lawdCd, dealYmd,
                AptTradeImportCommandFactory.HOUSE_TYPE, AptTradeImportCommandFactory.DEAL_TYPE,
                response.totalCount(), persistRows));
        return liveRows;
    }

    private List<LiveRow> fetchRent(String lawdCd, String sigungu, String dealYmd) {
        AptRentApiResponse response = pageFetcher.fetchRents(lawdCd, dealYmd);
        List<PersistRow> persistRows = new ArrayList<>();
        List<LiveRow> liveRows = new ArrayList<>();
        for (AptRentApiItem item : response.items()) {
            HouseUpsertCommand houseCommand = rentCommandFactory.toHouseCommand(lawdCd, null, item);
            HouseDealInsertCommand dealCommand = rentCommandFactory.toDealCommand(lawdCd, dealYmd, null, item);
            HouseSearchResultResponse result = toResponse(lawdCd, dealYmd, sigungu, houseCommand, dealCommand);
            persistRows.add(new PersistRow(lawdCd, sigungu, item.umdNm(), houseCommand, dealCommand));
            liveRows.add(new LiveRow(result, new AutoImportRangeResponse(lawdCd, dealYmd, "live",
                    AptRentImportCommandFactory.SOURCE_API + " live response")));
        }
        persistService.persistAsync(new PersistRequest(AptRentImportCommandFactory.SOURCE_API, lawdCd, dealYmd,
                AptRentImportCommandFactory.HOUSE_TYPE, AptRentImportCommandFactory.DEAL_TYPE,
                response.totalCount(), persistRows));
        return liveRows;
    }

    private HouseSearchResultResponse toResponse(
            String lawdCd,
            String dealYmd,
            String sigungu,
            HouseUpsertCommand houseCommand,
            HouseDealInsertCommand dealCommand
    ) {
        return new HouseSearchResultResponse(
                null,
                null,
                houseCommand.aptNm(),
                SeoulLawdCodeResolver.SEOUL_SIDO_NAME,
                sigungu,
                houseCommand.umdNm(),
                houseCommand.jibun(),
                houseCommand.buildYear(),
                lawdCd,
                dealYmd,
                dealCommand.dealType(),
                dealCommand.rentType(),
                dealCommand.dealDate(),
                dealCommand.dealAmount(),
                dealCommand.dealAmountManwon(),
                dealCommand.deposit(),
                dealCommand.depositManwon(),
                dealCommand.monthlyRent(),
                dealCommand.monthlyRentManwon(),
                dealCommand.excluUseAr(),
                dealCommand.floor(),
                dealCommand.contractTerm(),
                dealCommand.contractType(),
                dealCommand.useRRRight(),
                dealCommand.preDeposit(),
                dealCommand.preDepositManwon(),
                dealCommand.preMonthlyRent(),
                dealCommand.preMonthlyRentManwon(),
                dealCommand.roadnm(),
                dealCommand.aptSeq(),
                null,
                null,
                dealCommand.apiRowHash(),
                dealCommand.apiRowHash()
        );
    }

    private static boolean shouldFetchSale(String dealMode) {
        return "sale".equals(dealMode) || "all".equals(dealMode);
    }

    private static boolean shouldFetchRent(String dealMode) {
        return "jeonse".equals(dealMode) || "monthly".equals(dealMode)
                || "rent".equals(dealMode) || "all".equals(dealMode);
    }

    private record LiveRow(HouseSearchResultResponse response, AutoImportRangeResponse range) {
    }
}

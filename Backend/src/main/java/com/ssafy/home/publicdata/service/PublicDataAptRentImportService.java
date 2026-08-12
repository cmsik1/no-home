package com.ssafy.home.publicdata.service;

import com.ssafy.home.common.region.SeoulLawdCodeResolver;
import com.ssafy.home.publicdata.client.PublicDataAptRentClient;
import com.ssafy.home.publicdata.client.PublicDataAptRentXmlParser;
import com.ssafy.home.publicdata.dto.AptRentApiItem;
import com.ssafy.home.publicdata.dto.AptRentApiResponse;
import com.ssafy.home.publicdata.dto.PublicDataImportResult;
import com.ssafy.home.publicdata.persistence.HouseDealInsertCommand;
import com.ssafy.home.publicdata.persistence.HouseUpsertCommand;
import com.ssafy.home.publicdata.service.PublicDataBatchPersistService.PersistRequest;
import com.ssafy.home.publicdata.service.PublicDataBatchPersistService.PersistRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.ssafy.home.publicdata.service.AptRentImportCommandFactory.DEAL_TYPE;
import static com.ssafy.home.publicdata.service.AptRentImportCommandFactory.HOUSE_TYPE;
import static com.ssafy.home.publicdata.service.AptRentImportCommandFactory.SOURCE_API;

/**
 * 아파트 전월세 공공 API를 끝 page까지 수집하고 응답 item을 주택·거래 저장 command로 변환한다.
 * 매매 import와 같은 batch 저장 경계를 사용해 적재 상태와 중복 정책을 통일한다.
 */
@Service
public class PublicDataAptRentImportService {

    private static final int PAGE_SIZE = 1000;

    private final PublicDataAptRentClient client;
    private final PublicDataAptRentXmlParser parser;
    private final AptRentImportCommandFactory commandFactory;
    private final SeoulLawdCodeResolver seoulLawdCodeResolver;
    private final PublicDataBatchPersistService batchPersistService;

    @Autowired
    public PublicDataAptRentImportService(
            PublicDataAptRentClient client,
            PublicDataAptRentXmlParser parser,
            AptRentImportCommandFactory commandFactory,
            SeoulLawdCodeResolver seoulLawdCodeResolver,
            PublicDataBatchPersistService batchPersistService
    ) {
        this.client = client;
        this.parser = parser;
        this.commandFactory = commandFactory;
        this.seoulLawdCodeResolver = seoulLawdCodeResolver;
        this.batchPersistService = batchPersistService;
    }

    public PublicDataImportResult importAptRents(String lawdCd, String dealYmd) {
        if (!batchPersistService.prepare(SOURCE_API, lawdCd, dealYmd, HOUSE_TYPE, DEAL_TYPE)) {
            return new PublicDataImportResult(SOURCE_API, lawdCd, dealYmd, "success", 0, 0, 0, true,
                    "success batch already exists; skipped normal import");
        }
        try {
            ImportResult importResult = importAllPages(lawdCd, dealYmd);
            return batchPersistService.persist(new PersistRequest(SOURCE_API, lawdCd, dealYmd, HOUSE_TYPE, DEAL_TYPE,
                    importResult.totalCount, importResult.rows));
        } catch (RuntimeException exception) {
            batchPersistService.recordFailure(SOURCE_API, lawdCd, dealYmd, HOUSE_TYPE, DEAL_TYPE, exception);
            throw exception;
        }
    }

    private ImportResult importAllPages(String lawdCd, String dealYmd) {
        int totalCount = 0;
        int processedCount = 0;
        int pageNo = 1;
        List<PersistRow> rows = new java.util.ArrayList<>();

        while (true) {
            AptRentApiResponse response = parser.parse(client.fetchXml(lawdCd, dealYmd, pageNo, PAGE_SIZE));
            validateSuccess(response);
            if (pageNo == 1) {
                totalCount = response.totalCount();
            }
            if (response.items().isEmpty()) {
                break;
            }

            rows.addAll(importItems(lawdCd, dealYmd, response));
            processedCount += response.items().size();

            if (processedCount >= totalCount) {
                break;
            }
            pageNo++;
        }

        return new ImportResult(totalCount, rows);
    }

    private static void validateSuccess(AptRentApiResponse response) {
        if (response.isSuccess()) {
            return;
        }

        throw new PublicDataApiException(
                PublicDataImportService.classifyApiFailure(response.resultCode(), response.resultMsg()),
                response.resultCode(),
                response.resultMsg()
        );
    }

    private List<PersistRow> importItems(String lawdCd, String dealYmd, AptRentApiResponse response) {
        List<PersistRow> rows = new java.util.ArrayList<>();
        String sigungu = seoulLawdCodeResolver.sigunguName(lawdCd).orElse("");
        for (AptRentApiItem item : response.items()) {
            HouseUpsertCommand houseCommand = commandFactory.toHouseCommand(lawdCd, null, item);
            HouseDealInsertCommand dealCommand = commandFactory.toDealCommand(lawdCd, dealYmd, null, item);
            rows.add(new PersistRow(lawdCd, sigungu, item.umdNm(), houseCommand, dealCommand));
        }
        return rows;
    }

    private static class ImportResult {
        private final int totalCount;
        private final List<PersistRow> rows;

        private ImportResult(int totalCount, List<PersistRow> rows) {
            this.totalCount = totalCount;
            this.rows = rows;
        }
    }
}

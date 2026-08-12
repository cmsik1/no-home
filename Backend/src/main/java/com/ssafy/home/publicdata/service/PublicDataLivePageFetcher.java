package com.ssafy.home.publicdata.service;

import com.ssafy.home.publicdata.client.PublicDataAptRentClient;
import com.ssafy.home.publicdata.client.PublicDataAptRentXmlParser;
import com.ssafy.home.publicdata.client.PublicDataAptTradeClient;
import com.ssafy.home.publicdata.client.PublicDataAptTradeXmlParser;
import com.ssafy.home.publicdata.dto.AptRentApiItem;
import com.ssafy.home.publicdata.dto.AptRentApiResponse;
import com.ssafy.home.publicdata.dto.AptTradeApiItem;
import com.ssafy.home.publicdata.dto.AptTradeApiResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 실시간 검색에 필요한 공공 API page를 total count까지 수집한다.
 * client의 XML 문자열을 parser 응답으로 바꾸고 provider result code를 application 예외로 변환한다.
 */
@Component
public class PublicDataLivePageFetcher {

    private static final int PAGE_SIZE = 1000;

    private final PublicDataAptTradeClient tradeClient;
    private final PublicDataAptTradeXmlParser tradeParser;
    private final PublicDataAptRentClient rentClient;
    private final PublicDataAptRentXmlParser rentParser;

    public PublicDataLivePageFetcher(
            PublicDataAptTradeClient tradeClient,
            PublicDataAptTradeXmlParser tradeParser,
            PublicDataAptRentClient rentClient,
            PublicDataAptRentXmlParser rentParser
    ) {
        this.tradeClient = tradeClient;
        this.tradeParser = tradeParser;
        this.rentClient = rentClient;
        this.rentParser = rentParser;
    }

    public AptTradeApiResponse fetchTrades(String lawdCd, String dealYmd) {
        AptTradeApiResponse first = tradeParser.parse(tradeClient.fetchXml(lawdCd, dealYmd, 1, PAGE_SIZE));
        validate(first.resultCode(), first.resultMsg(), first.isSuccess());
        List<AptTradeApiItem> items = new ArrayList<>(first.items());
        for (int pageNo = 2; items.size() < first.totalCount(); pageNo++) {
            AptTradeApiResponse page = tradeParser.parse(tradeClient.fetchXml(lawdCd, dealYmd, pageNo, PAGE_SIZE));
            validate(page.resultCode(), page.resultMsg(), page.isSuccess());
            items.addAll(page.items());
            if (page.items().isEmpty()) {
                break;
            }
        }
        return new AptTradeApiResponse(first.resultCode(), first.resultMsg(), first.totalCount(), items);
    }

    public AptRentApiResponse fetchRents(String lawdCd, String dealYmd) {
        AptRentApiResponse first = rentParser.parse(rentClient.fetchXml(lawdCd, dealYmd, 1, PAGE_SIZE));
        validate(first.resultCode(), first.resultMsg(), first.isSuccess());
        List<AptRentApiItem> items = new ArrayList<>(first.items());
        for (int pageNo = 2; items.size() < first.totalCount(); pageNo++) {
            AptRentApiResponse page = rentParser.parse(rentClient.fetchXml(lawdCd, dealYmd, pageNo, PAGE_SIZE));
            validate(page.resultCode(), page.resultMsg(), page.isSuccess());
            items.addAll(page.items());
            if (page.items().isEmpty()) {
                break;
            }
        }
        return new AptRentApiResponse(first.resultCode(), first.resultMsg(), first.totalCount(), items);
    }

    private static void validate(String resultCode, String resultMsg, boolean success) {
        if (!success) {
            throw new PublicDataApiException(
                    PublicDataImportService.classifyApiFailure(resultCode, resultMsg),
                    resultCode,
                    resultMsg
            );
        }
    }
}

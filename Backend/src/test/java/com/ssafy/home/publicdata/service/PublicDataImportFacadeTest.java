package com.ssafy.home.publicdata.service;

import com.ssafy.home.publicdata.dto.PublicDataImportResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PublicDataImportFacadeTest {

    private final PublicDataImportService tradeService = mock(PublicDataImportService.class);
    private final PublicDataAptRentImportService rentService = mock(PublicDataAptRentImportService.class);
    private final PublicDataImportFacade facade = new PublicDataImportFacade(tradeService, rentService);

    @Test
    void saleDelegatesToTradeImport() {
        PublicDataImportResult expected = result("trade");
        when(tradeService.importAptTrades("11590", "202405")).thenReturn(expected);

        assertThat(facade.importAptDeals("11590", "202405", "sale")).isSameAs(expected);
        verifyNoInteractions(rentService);
    }

    @Test
    void allImportsTradeThenReturnsRentResultForExistingApiContract() {
        PublicDataImportResult expected = result("rent");
        when(rentService.importAptRents("11590", "202405")).thenReturn(expected);

        assertThat(facade.importAptDeals("11590", "202405", "all")).isSameAs(expected);
        verify(tradeService).importAptTrades("11590", "202405");
    }

    @Test
    void unsupportedModeIsRejected() {
        assertThatThrownBy(() -> facade.importAptDeals("11590", "202405", "unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported dealMode");
    }

    private static PublicDataImportResult result(String source) {
        return new PublicDataImportResult(source, "11590", "202405", "success", 1, 1, 0, false, "done");
    }
}

package com.ssafy.home.publicdata.controller;

import com.ssafy.home.common.feature.FeatureDisabledException;
import com.ssafy.home.common.response.GlobalExceptionHandler;
import com.ssafy.home.publicdata.dto.PublicDataImportResult;
import com.ssafy.home.publicdata.service.PublicDataImportFacade;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class PublicDataImportControllerTest {

    @Test
    void delegatesImportToFacade() throws Exception {
        PublicDataImportFacade facade = mock(PublicDataImportFacade.class);
        when(facade.importAptDeals("11590", "202405", "sale"))
                .thenReturn(new PublicDataImportResult("trade", "11590", "202405", "success",
                        1, 1, 0, false, "done"));
        MockMvc mockMvc = mockMvc(facade);

        mockMvc.perform(post("/api/public-data/apt-trades/import")
                        .param("lawdCd", "11590")
                        .param("dealYmd", "202405")
                        .param("dealMode", "sale"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("success"));

        verify(facade).importAptDeals("11590", "202405", "sale");
    }

    @Test
    void invalidModeUsesCommonBadRequestResponse() throws Exception {
        PublicDataImportFacade facade = mock(PublicDataImportFacade.class);
        when(facade.importAptDeals("11590", "202405", "unknown"))
                .thenThrow(new IllegalArgumentException("Unsupported dealMode option: unknown"));

        mockMvc(facade).perform(post("/api/public-data/apt-trades/import")
                        .param("lawdCd", "11590")
                        .param("dealYmd", "202405")
                        .param("dealMode", "unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void disabledManualImportReturnsServiceUnavailable() throws Exception {
        PublicDataImportFacade facade = mock(PublicDataImportFacade.class);
        when(facade.importAptDeals("11590", "202405", "sale"))
                .thenThrow(new FeatureDisabledException());

        mockMvc(facade).perform(post("/api/public-data/apt-trades/import")
                        .param("lawdCd", "11590")
                        .param("dealYmd", "202405")
                        .param("dealMode", "sale"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("FEATURE_DISABLED"));
    }

    private static MockMvc mockMvc(PublicDataImportFacade facade) {
        return standaloneSetup(new PublicDataImportController(facade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }
}

package com.ssafy.home.common.web;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ApiNoStoreFilterTest {

    private final ApiNoStoreFilter filter = new ApiNoStoreFilter();

    @Test
    void addsNoStoreToSuccessfulApiResponse() throws Exception {
        MockHttpServletResponse response = filter("/api/health", HttpServletResponse.SC_OK);

        assertThat(response.getHeader(HttpHeaders.CACHE_CONTROL)).isEqualTo("no-store");
    }

    @Test
    void addsNoStoreToFailedApiResponse() throws Exception {
        MockHttpServletResponse response = filter("/api/members/me", HttpServletResponse.SC_UNAUTHORIZED);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getHeader(HttpHeaders.CACHE_CONTROL)).isEqualTo("no-store");
    }

    @Test
    void leavesNonApiResponseUnchanged() throws Exception {
        MockHttpServletResponse response = filter("/index.html", HttpServletResponse.SC_OK);

        assertThat(response.getHeader(HttpHeaders.CACHE_CONTROL)).isNull();
    }

    private MockHttpServletResponse filter(String path, int status) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (servletRequest, servletResponse) ->
                ((HttpServletResponse) servletResponse).setStatus(status);

        filter.doFilter(request, response, chain);
        return response;
    }
}

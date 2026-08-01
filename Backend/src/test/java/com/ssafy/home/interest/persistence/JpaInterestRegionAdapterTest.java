package com.ssafy.home.interest.persistence;

import com.ssafy.home.interest.dto.InterestRegion;
import com.ssafy.home.interest.repository.InterestRegionEntity;
import com.ssafy.home.interest.repository.InterestRegionRepository;
import com.ssafy.home.test.PostgresIntegrationTest;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaInterestRegionAdapter.class)
class JpaInterestRegionAdapterTest extends PostgresIntegrationTest {

    @Autowired
    private InterestRegionPersistencePort mapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private InterestRegionRepository interestRegionRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void insertSelectAndDeleteInterestRegion() {
        Long memberId = insertMember();
        mapper.insertRegion("11590", "1159010500", "서울특별시", "동작구", "흑석동");
        Long regionId = mapper.selectRegionId("11590", "흑석동").orElseThrow();

        int inserted = mapper.insertInterestRegion(memberId, regionId);
        int duplicated = mapper.insertInterestRegion(memberId, regionId);
        List<InterestRegion> regions = mapper.selectByMemberId(memberId);
        int deleted = mapper.deleteInterestRegion(memberId, regions.get(0).interestRegionId());

        assertThat(inserted).isEqualTo(1);
        assertThat(duplicated).isZero();
        assertThat(regions).extracting(InterestRegion::umdNm).containsExactly("흑석동");
        assertThat(deleted).isEqualTo(1);
        assertThat(mapper.selectByMemberId(memberId)).isEmpty();
    }

    @Test
    void loadsRegionAssociationForDtoMappingWhenOpenInViewIsDisabled() {
        Long memberId = insertMember();
        mapper.insertRegion("11590", "1159010500", "서울특별시", "동작구", "흑석동");
        Long regionId = mapper.selectRegionId("11590", "흑석동").orElseThrow();
        mapper.insertInterestRegion(memberId, regionId);

        InterestRegionEntity entity = interestRegionRepository
                .findAllByMemberIdOrderByRegionSidoAscRegionSigunguAscRegionUmdNmAsc(memberId)
                .get(0);

        assertThat(entityManagerFactory.getPersistenceUnitUtil().isLoaded(entity.getRegion())).isTrue();
    }

    private Long insertMember() {
        jdbcTemplate.update("""
                INSERT INTO members (email, password_hash, name, phone)
                VALUES ('interest@example.com', 'hash', 'Interest User', '010')
                """);
        return jdbcTemplate.queryForObject("SELECT member_id FROM members WHERE email = 'interest@example.com'", Long.class);
    }
}

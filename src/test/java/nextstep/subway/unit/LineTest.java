package nextstep.subway.unit;



import static org.assertj.core.api.AssertionsForInterfaceTypes.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import nextstep.subway.domain.Line;
import nextstep.subway.domain.Section;
import nextstep.subway.domain.Station;

class LineTest {

    private Line 이호선;
    private Station 강남역;
    private Station 역삼역;

    @BeforeEach
    void setUp() {
        이호선 = new Line("2호선", "bg-color-green");
        강남역 = new Station("강남역");
        역삼역 = new Station("역삼역");
    }

    /**
     * given 노선과 2개의 역이 존재할 때
     * when 2개의 역으로 구간을 추가하면
     * then 노선에 구간 정보가 추가된다.
     */
    @Test
    @DisplayName("구간 추가 테스트")
    void addSection() {
        Section 구간1 = 이호선_구간1_생성();
        이호선.addSection(구간1);

        assertThat(이호선.getSections()).containsExactly(구간1);
    }

    /**
     * given 노선에 2개의 역이 존재할 때,
     * when 노선의 역을 조회하면
     * then 구간에 포함된 역이 조회된다.
     */
    @Test
    @DisplayName("호선의 역 확인 테스트")
    void getStations() {
        // given
        Section 구간1 = 이호선_구간1_생성();
        이호선.addSection(구간1);

        // when
        List<Station> stations = 이호선.getStations();

        // then
        assertThat(stations).contains(강남역, 역삼역);
    }

    /**
     * when 노선의 구간이 주어졌을 때,
     * given 해당 구간을 삭제하면
     * then 구간이 조회되지 않는다.
     */
    @Test
    @DisplayName("구간 삭제 테스트")
    void removeSection() {
        // given
        Section 구간1 = 이호선_구간1_생성();
        이호선.addSection(구간1);

        // when
        이호선.removeSection(구간1.getDownStation());

        // then
        List<Station> stations = 이호선.getStations();
        assertThat(stations).isEmpty();
    }

    /**
     * when 노선의 구간이 존재하지 않을 때,
     * given 구간을 삭제하면
     * then 구간이 조회되지 않는다.
     */
    @Test
    @DisplayName("빈 구간 삭제시 예외를 던진다")
    void removeEmptySection() {
        Section 구간1 = 이호선_구간1_생성();

        // when
        assertThatThrownBy(() -> 이호선.removeSection(구간1.getDownStation()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("구간이 존재하지 않습니다.");
    }

    private Section 이호선_구간1_생성() {
        return new Section(이호선, 강남역, 역삼역, 10);
    }

}

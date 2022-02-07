package nextstep.subway.acceptance;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import static nextstep.subway.acceptance.LineSteps.*;
import static nextstep.subway.acceptance.StationSteps.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 구간 관리 기능")
class LineSectionAcceptanceTest extends AcceptanceTest {
    private static final int DEFAULT_DISTANCE = 5;

    private Long 신분당선;

    private Long 강남역;
    private Long 양재역;
    private Long 양재시민의숲;
    private Long 판교역;
    private Long 광교역;

    /**
     * Given 지하철역과 노선 생성을 요청 하고
     */
    @BeforeEach
    public void setUp() {
        super.setUp();

        광교역 = 지하철역_식별번호_가져오기(지하철역_생성_요청("광교역"));
        양재시민의숲 = 지하철역_식별번호_가져오기(지하철역_생성_요청("양재시민의숲"));
        양재역 = 지하철역_식별번호_가져오기(지하철역_생성_요청("양재역"));
        판교역 = 지하철역_식별번호_가져오기(지하철역_생성_요청("판교역"));
        강남역 = 지하철역_식별번호_가져오기(지하철역_생성_요청("강남역"));

        Map<String, String> lineCreateParams = createLineCreateParams(강남역, 양재시민의숲);
        신분당선 = 노선_식별번호_추출(지하철_노선_생성_요청(lineCreateParams));
    }

    /**
     * When 지하철 노선(강남역-양재시민의숲)에 새로운 구간 추가(양재시민의숲-정자역)를 요청 하면
     * Then 노선에 새로운 구간이 추가된다
     */
    @DisplayName("지하철 노선에 구간을 등록")
    @Test
    void addLineSection() {
        // when
        Long 정자역 = 지하철역_식별번호_가져오기(지하철역_생성_요청("정자역"));
        지하철_노선에_지하철_구간_생성_요청(신분당선, createSectionCreateParams(양재시민의숲, 정자역));

        // then
        ExtractableResponse<Response> response = 지하철_노선_조회_요청(신분당선);
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(지하철역_식별번호_내역들_가져오기(response)).containsExactly(강남역, 양재시민의숲, 정자역);
    }

    /**
     * When 지하철 노선(강남역-양재시민의숲)에 하나라도 등록되지 않은 역(판교역-광교역)으로 구간 요청을 하면
     * Then 노선에 구간 추가가 실패한다.
     */
    @DisplayName("지하철 노선에 등록되지 않은 역으로 구간 추가 요청하면 실패한다.")
    @Test
    void addSectionNoneMatchStationException() {
        // when
        ExtractableResponse<Response> response = 지하철_노선에_지하철_구간_생성_요청(신분당선, createSectionCreateParams(판교역, 광교역));

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }


    /**
     * When 지하철 노선(강남역-양재시민의숲) 중간에 새로운 구간(양재역-양재시민의숲)을 잘못된 거리로 추가 요청하면
     * Then 노선에 구간 추가가 실패한다.
     */
    @DisplayName("지하철 노선 중간에 구간을 요청할 때 거리가 잘못되면 실패한다.")
    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 10})
    void addSectionBadDistance(int distance) {
        // when
        ExtractableResponse<Response> response = 지하철_노선에_지하철_구간_생성_요청(신분당선, createSectionCreateParams(양재역, 양재시민의숲, distance));

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * Given 지하철 노선에 새로운 구간(양재시민의숲-판교역)을 요청한다
     * Given 지하철 노선에 새로운 구간(판교역-광교역)을 요청한다
     * When 지하철 노선(강남역-양재시민의숲-판교역-광교역)에 모두 등록된 역(강남역-광교역)으로 구간 요청을 하면
     * Then 노선에 구간 추가가 실패한다.
     */
    @DisplayName("지하철 노선에 모두 등록된 역으로 구간 요청하면 실패한다.")
    @Test
    void addSectionAllMatchStationException() {
        // Given
        지하철_노선에_지하철_구간_생성_요청(신분당선, createSectionCreateParams(양재시민의숲, 판교역));
        지하철_노선에_지하철_구간_생성_요청(신분당선, createSectionCreateParams(판교역, 광교역));

        // when
        ExtractableResponse<Response> response = 지하철_노선에_지하철_구간_생성_요청(신분당선, createSectionCreateParams(판교역, 광교역));

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * When 지하철 노선(강남역 - 양재시민의숲역) 구간에 상행역(강남역) 중심으로 새로운 구간(강남역 - 양재역)을 추가 요청 하면
     * Then 노선에 새로운 구간이 추가된다
     */
    @DisplayName("지하철 노선 중간에 상행역 중심으로 새로운 구간을 등록")
    @Test
    void addSectionOnTheUpStation() {
        // when
        지하철_노선에_지하철_구간_생성_요청(신분당선, createSectionCreateParams(강남역, 양재역));

        // then
        ExtractableResponse<Response> response = 지하철_노선_조회_요청(신분당선);
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(지하철역_식별번호_내역들_가져오기(response)).containsExactly(강남역, 양재역, 양재시민의숲);
    }

    /**
     * When 지하철 노선(강남역 - 양재시민의숲역) 구간에 하행역(양재시민의숲역) 중심으로 새로운 구간(양재역 - 양재시민의숲역)을 추가 요청 하면
     * Then 노선에 새로운 구간이 추가된다
     */
    @DisplayName("지하철 노선 중간에 상행역 중심으로 새로운 구간을 등록")
    @Test
    void addSectionOnTheDownStation() {
        // when
        지하철_노선에_지하철_구간_생성_요청(신분당선, createSectionCreateParams(양재역, 양재시민의숲));

        // then
        ExtractableResponse<Response> response = 지하철_노선_조회_요청(신분당선);
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(지하철역_식별번호_내역들_가져오기(response)).containsExactly(강남역, 양재역, 양재시민의숲);
    }

    /**
     * Given 지하철 노선(강남역 - 양재시민의숲역) 구간에 하행역(양재시민의숲역) 중심으로 새로운 구간(양재역 - 양재시민의숲역)을 추가 요청하고
     * Given 지하철 노선(양재역 - 양재시민의숲역) 구간에 하행역(양재시민의숲역) 중심으로 새로운 구간(양재시민의숲역 - 판교역)을 추가 요청하고
     * When 선택한 지하철 노선을 조회 요청하면
     * Then 노선의 구간이 정렬(강남역-양재역-양재시민의숲-판교역)되어 조회된다
     */
    @DisplayName("지하철 노선에 구간을 집어넣으면 정렬되어서 조회")
    @Test
    void sortStations() {
        // given
        지하철_노선에_지하철_구간_생성_요청(신분당선, createSectionCreateParams(양재역, 양재시민의숲));
        지하철_노선에_지하철_구간_생성_요청(신분당선, createSectionCreateParams(양재시민의숲, 판교역));

        // when
        ExtractableResponse<Response> response = 지하철_노선_조회_요청(신분당선);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(지하철역_식별번호_내역들_가져오기(response)).containsExactly(강남역, 양재역, 양재시민의숲, 판교역);
    }

    /**
     * Given 지하철 노선(강남역-양재시민의숲)에 새로운 구간 추가(양재시민의숲-정자역)를 요청 하고
     * When 지하철 노선의 마지막 구간(정자역) 제거를 요청 하면
     * Then 노선에 구간이 제거된다
     */
    @DisplayName("지하철 노선의 마지막 구간을 제거")
    @Test
    void removeLastSection() {
        // given
        Long 정자역 = 지하철역_생성_요청("정자역").jsonPath().getLong("id");
        지하철_노선에_지하철_구간_생성_요청(신분당선, createSectionCreateParams(양재시민의숲, 정자역));

        // when
        지하철_노선에_지하철_구간_제거_요청(신분당선, 정자역);

        // then
        ExtractableResponse<Response> response = 지하철_노선_조회_요청(신분당선);
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(지하철역_식별번호_내역들_가져오기(response)).containsExactly(강남역, 양재시민의숲);
    }

    /**
     * Given 지하철 노선(강남역-양재시민의숲)에 새로운 구간 추가(양재시민의숲-양재역)를 요청 하고
     * When 지하철 노선의 중간 구간(양재시민의숲) 제거를 요청 하면
     * Then 노선에 중간 구간이 제거된다
     */
    @DisplayName("지하철 노선의 구간 3개일 때 중간 구간 하나 제거")
    @Test
    void removeMiddleSection01() {
        // given
        지하철_노선에_지하철_구간_생성_요청(신분당선, createSectionCreateParams(양재시민의숲, 양재역));

        // when
        지하철_노선에_지하철_구간_제거_요청(신분당선, 양재시민의숲);

        // then
        ExtractableResponse<Response> response = 지하철_노선_조회_요청(신분당선);
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(지하철역_식별번호_내역들_가져오기(response)).containsExactly(강남역, 양재역);
    }


    /**
     * Given 지하철 노선(강남역-양재시민의숲)에 새로운 구간 추가(양재시민의숲-양재역)를 요청 하고
     * Given 지하철 노선(강남역-양재시민의숲-양재역)에 새로운 구간 추가(양재역-판교역)를 요청 하고
     * When 지하철 노선의 중간 구간(양재역) 제거를 요청 하면
     * Then 노선에 중간 구간이 제거된다
     */
    @DisplayName("지하철 노선의 구간 4개일 때 중간 구간 하나 제거")
    @Test
    void removeMiddleSection02() {
        // given
        지하철_노선에_지하철_구간_생성_요청(신분당선, createSectionCreateParams(양재시민의숲, 양재역));
        지하철_노선에_지하철_구간_생성_요청(신분당선, createSectionCreateParams(양재역, 판교역));

        // when
        지하철_노선에_지하철_구간_제거_요청(신분당선, 양재역);

        // then
        ExtractableResponse<Response> response = 지하철_노선_조회_요청(신분당선);
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(지하철역_식별번호_내역들_가져오기(response)).containsExactly(강남역, 양재시민의숲, 판교역);
    }


    /**
     * Given 지하철 노선(강남역-양재시민의숲)에 새로운 구간 추가(양재시민의숲-양재역)를 요청 하고
     * Given 지하철 노선(강남역-양재시민의숲-양재역)에 새로운 구간 추가(양재역-판교역)를 요청 하고
     * When 지하철 노선의 중간 구간(양재시민의숲) 제거를 요청 하면
     * When 지하철 노선의 중간 구간(양재역) 제거를 요청 하면
     * Then 노선에 중간 구간이 제거된다
     */
    @DisplayName("지하철 노선의 구간이 여러개일 때 모든 중간 구간 제거")
    @Test
    void removeAllMiddleSection() {
        // given
        지하철_노선에_지하철_구간_생성_요청(신분당선, createSectionCreateParams(양재시민의숲, 양재역));
        지하철_노선에_지하철_구간_생성_요청(신분당선, createSectionCreateParams(양재역, 판교역));

        // when
        지하철_노선에_지하철_구간_제거_요청(신분당선, 양재시민의숲);
        지하철_노선에_지하철_구간_제거_요청(신분당선, 양재역);

        // then
        ExtractableResponse<Response> response = 지하철_노선_조회_요청(신분당선);
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(지하철역_식별번호_내역들_가져오기(response)).containsExactly(강남역, 판교역);
    }

    /**
     * When 지하철 노선의 구간 길이가 최소 1개 이하일 때 제거 요청하면
     * Then 노선의 구간 제거 요청이 실패한다.
     */
    @DisplayName("지하철 노선의 구간이 1개 이하일 때 제거 요청하면 예외처리")
    @Test
    void removeSectionMinimumSizeException() {
        // when
        ExtractableResponse<Response> response = 지하철_노선에_지하철_구간_제거_요청(신분당선, 양재시민의숲);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * Given 지하철 노선(강남역-양재시민의숲)에 새로운 구간 추가(양재시민의숲-양재역)를 요청 하고
     * When 지하철 노선에 존재하지 않은 구간(광교역) 제거를 요청하면
     * Then 구간 삭제 요청이 실패한다.
     */
    @DisplayName("지하철 노선에 존재하지 않은 구간을 제거")
    @Test
    void removeSectionNoneStationException() {
        // given
        지하철_노선에_지하철_구간_생성_요청(신분당선, createSectionCreateParams(양재시민의숲, 양재역));

        // when
        ExtractableResponse<Response> response = 지하철_노선에_지하철_구간_제거_요청(신분당선, 광교역);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private Map<String, String> createLineCreateParams(Long upStationId, Long downStationId) {
        Map<String, String> lineCreateParams;
        lineCreateParams = new HashMap<>();
        lineCreateParams.put("name", "신분당선");
        lineCreateParams.put("color", "bg-red-600");
        lineCreateParams.put("upStationId", upStationId + "");
        lineCreateParams.put("downStationId", downStationId + "");
        lineCreateParams.put("distance", 10 + "");
        return lineCreateParams;
    }

    private Map<String, String> createSectionCreateParams(Long upStationId, Long downStationId) {
        return createSectionCreateParams(upStationId, downStationId, DEFAULT_DISTANCE);
    }

    private Map<String, String> createSectionCreateParams(Long upStationId, Long downStationId, int distance) {
        Map<String, String> params = new HashMap<>();
        params.put("upStationId", upStationId + "");
        params.put("downStationId", downStationId + "");
        params.put("distance", distance + "");
        return params;
    }
}

package nextstep.api.unit.subway.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static nextstep.api.unit.subway.LineFixture.DEFAULT_LINE_DISTANCE;
import static nextstep.api.unit.subway.LineFixture.DEFAULT_LINE_DURATION;
import static nextstep.api.unit.subway.LineFixture.makeLine;
import static nextstep.api.unit.subway.StationFixture.강남역;
import static nextstep.api.unit.subway.StationFixture.교대역;
import static nextstep.api.unit.subway.StationFixture.삼성역;
import static nextstep.api.unit.subway.StationFixture.선릉역;
import static nextstep.api.unit.subway.StationFixture.역삼역;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import nextstep.api.subway.domain.line.Section;
import nextstep.api.subway.domain.line.exception.LineAppendSectionException;
import nextstep.api.subway.domain.line.exception.LineRemoveSectionException;

class LineTest {

    @DisplayName("구간을 추가한다")
    @Nested
    class AppendSectionTest {

        @Nested
        class Success {

            @Test
            void 노선_앞에_구간을_추가한다() {
                final var line = makeLine(강남역, 역삼역);

                final var section = new Section(교대역, 강남역, 10, 10);
                line.appendSection(section);

                final var actual = line.getStations();
                assertThat(actual).contains(교대역, 강남역, 역삼역);
            }

            @Test
            void 노선_뒤에_구간을_추가한다() {
                final var line = makeLine(강남역, 역삼역);

                final var section = new Section(역삼역, 선릉역, 10, 10);
                line.appendSection(section);

                final var actual = line.getStations();
                assertThat(actual).contains(강남역, 역삼역, 선릉역);
            }

            @Nested
            class 노선_중간에_구간을_추가한다 {

                @Test
                void 상행역이_동일한_구간을_추가한다() {
                    final var line = makeLine(강남역, 선릉역);

                    final var section = new Section(강남역, 역삼역, 5, 5);
                    line.appendSection(section);

                    final var actual = line.getStations();
                    assertThat(actual).contains(강남역, 역삼역, 선릉역);
                }

                @Test
                void 하행역이_동일한_구간을_추가한다() {
                    final var line = makeLine(강남역, 선릉역);

                    final var section = new Section(역삼역, 선릉역, 5, 5);
                    line.appendSection(section);

                    final var actual = line.getStations();
                    assertThat(actual).contains(강남역, 역삼역, 선릉역);
                }
            }
        }

        @Nested
        class Fail {

            @Test
            void 상행역과_하행역_모두_노선에_포함되어_있으면_안된다() {
                final var line = makeLine(강남역, 역삼역, 선릉역);

                final var section = new Section(강남역, 선릉역, 15, 10);
                assertThatThrownBy(() -> line.appendSection(section))
                        .isInstanceOf(LineAppendSectionException.class);
            }

            @Test
            void 상행역과_하행역_둘중_하나도_노선에_포함되어_있지_않으면_안된다() {
                final var line = makeLine(강남역, 역삼역, 선릉역);

                final var section = new Section(선릉역, 강남역, 10, 10);
                assertThatThrownBy(() -> line.appendSection(section))
                        .isInstanceOf(LineAppendSectionException.class);
            }

            @ParameterizedTest
            @ValueSource(ints = {DEFAULT_LINE_DISTANCE, DEFAULT_LINE_DISTANCE + 1})
            void 역_사이에_새로운_역을_등록할_경우_새로운_구간의_길이는_기존_역_사이_길이보다_크거나_같아선_안된다(final int length) {
                final var line = makeLine(강남역, 선릉역);
                final var section = new Section(강남역, 역삼역, length, 10);

                assertThatThrownBy(() -> line.appendSection(section))
                        .isInstanceOf(LineAppendSectionException.class);
            }

            @ParameterizedTest
            @ValueSource(ints = {DEFAULT_LINE_DURATION, DEFAULT_LINE_DURATION + 1})
            void 역_사이에_새로운_역을_등록할_경우_새로운_구간의_소요시간이_기존_역_사이의_소요시간보다_크거나_같아선_안된다(final int duration) {
                final var line = makeLine(강남역, 선릉역);
                final var section = new Section(강남역, 역삼역, 10, duration);

                assertThatThrownBy(() -> line.appendSection(section))
                        .isInstanceOf(LineAppendSectionException.class);
            }
        }
    }

    @DisplayName("구간을 삭제한다")
    @Nested
    class RemoveSectionTest {

        @Nested
        class Success {

            @Test
            void 상행_종점역을_삭제한다() {
                final var line = makeLine(강남역, 역삼역, 선릉역);
                line.removeSection(강남역);

                final var actual = line.getStations();
                assertThat(actual).contains(역삼역, 선릉역);
            }

            @Test
            void 하행_종점역을_삭제한다() {
                final var line = makeLine(강남역, 역삼역, 선릉역);
                line.removeSection(선릉역);

                final var actual = line.getStations();
                assertThat(actual).contains(강남역, 역삼역);
            }

            @Test
            void 중간역을_삭제한다() {
                final var line = makeLine(강남역, 역삼역, 선릉역);
                line.removeSection(역삼역);

                final var actual = line.getStations();
                assertThat(actual).contains(강남역, 선릉역);
            }
        }

        @Nested
        class Fail {

            @Test
            void 삭제하고자_하는_역은_노선에_존재하는_역이어야만_한다() {
                final var line = makeLine(강남역, 역삼역, 선릉역);

                assertThatThrownBy(() -> line.removeSection(삼성역))
                        .isInstanceOf(LineRemoveSectionException.class);
            }

            @Test
            void 노선의_구간이_하나만_있는_경우_역을_삭제할_수_없다() {
                final var line = makeLine(강남역, 역삼역);

                assertThatThrownBy(() -> line.removeSection(역삼역))
                        .isInstanceOf(LineRemoveSectionException.class);
            }
        }
    }

    @DisplayName("구간 내 역 목록을 반환한다")
    @Test
    void getStations() {
        final var line = makeLine(강남역, 역삼역, 선릉역, 삼성역);

        final var actual = line.getStations();
        assertThat(actual).contains(강남역, 역삼역, 선릉역, 삼성역);
    }
}

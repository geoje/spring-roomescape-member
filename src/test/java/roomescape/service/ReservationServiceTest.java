package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.controller.reservation.ReservationRequest;
import roomescape.controller.reservation.ReservationResponse;
import roomescape.controller.theme.ReservationThemeResponse;
import roomescape.controller.time.TimeResponse;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.H2ReservationRepository;
import roomescape.repository.H2ReservationTimeRepository;
import roomescape.repository.H2ThemeRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.exception.PreviousTimeException;
import roomescape.service.exception.ReservationDuplicatedException;
import roomescape.service.exception.ReservationNotFoundException;
import roomescape.service.exception.ThemeNotFoundException;
import roomescape.service.exception.TimeNotFoundException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import({ReservationService.class, H2ReservationRepository.class, H2ReservationTimeRepository.class, H2ThemeRepository.class})
class ReservationServiceTest {

    List<ReservationTime> sampleTimes = List.of(
            new ReservationTime(null, "08:00"),
            new ReservationTime(null, "09:10")
    );
    List<Theme> sampleThemes = List.of(
            new Theme(null, "Theme 1", "Description 1", "Thumbnail 1"),
            new Theme(null, "Theme 2", "Description 2", "Thumbnail 2")
    );
    List<ReservationRequest> sampleReservations = List.of(
            new ReservationRequest(
                    "Name 1",
                    LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE),
                    null,
                    null
            ),
            new ReservationRequest(
                    "Name 1",
                    LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE),
                    null,
                    null
            )
    );

    @Autowired
    ReservationService reservationService;
    @Autowired
    ReservationTimeRepository reservationTimeRepository;
    @Autowired
    ThemeRepository themeRepository;

    @BeforeEach
    void setUp() {
        sampleTimes = sampleTimes.stream()
                .map(reservationTimeRepository::save)
                .toList();
        sampleThemes = sampleThemes.stream()
                .map(themeRepository::save)
                .toList();
        sampleReservations = IntStream.range(0, sampleReservations.size())
                .mapToObj(i -> new ReservationRequest(
                        sampleReservations.get(i).name(),
                        sampleReservations.get(i).date(),
                        sampleTimes.get(i % sampleTimes.size()).getId(),
                        sampleThemes.get(i % sampleThemes.size()).getId()
                )).toList();
    }

    @Test
    @DisplayName("예약 목록을 조회한다.")
    void getReservations() {
        // given
        final List<ReservationResponse> expected = sampleReservations.stream()
                .map(reservationService::addReservation)
                .toList();

        // given & when
        final List<ReservationResponse> actual = reservationService.getReservations();

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("예약을 추가한다.")
    void addReservation() {
        // given
        final ReservationRequest reservationRequest = sampleReservations.get(0);

        // when
        final ReservationResponse actual = reservationService.addReservation(reservationRequest);

        final Optional<ReservationTime> timeOptional = sampleTimes.stream().filter(time -> time.getId().equals(reservationRequest.timeId())).findAny();
        final Optional<Theme> themeOptional = sampleThemes.stream().filter(theme -> theme.getId().equals(reservationRequest.themeId())).findAny();
        assertThat(timeOptional).isPresent();
        assertThat(themeOptional).isPresent();

        final ReservationResponse expected = new ReservationResponse(
                actual.id(),
                reservationRequest.name(),
                reservationRequest.date(),
                TimeResponse.from(timeOptional.get(), false),
                ReservationThemeResponse.from(themeOptional.get())
        );

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("예약을 삭제한다.")
    void deleteReservation() {
        // given
        final ReservationRequest reservationRequest = sampleReservations.get(0);

        // when
        final ReservationResponse actual = reservationService.addReservation(reservationRequest);

        // then
        assertThat(reservationService.deleteReservation(actual.id())).isOne();
        assertThatThrownBy(() -> reservationService.deleteReservation(actual.id()))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 시간으로 예약을 할 때 예외가 발생한다.")
    void exceptionOnAddingReservationWithNonExistTime() {
        // given
        final String tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
        final Long notExistTimeId = sampleTimes.stream()
                .map(ReservationTime::getId)
                .max(Long::compare).stream()
                .findAny()
                .orElseThrow() + 1;
        final Long themeId = sampleThemes.get(0).getId();
        final ReservationRequest request = new ReservationRequest("PK", tomorrow, notExistTimeId, themeId);

        // when & then
        assertThatThrownBy(() -> reservationService.addReservation(request))
                .isInstanceOf(TimeNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 테마로 예약을 할 때 예외가 발생한다.")
    void exceptionOnAddingReservationWithNonExistTheme() {
        // given
        final String tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
        final Long timeId = sampleTimes.get(0).getId();
        final Long notExistThemeId = sampleThemes.stream()
                .map(Theme::getId)
                .max(Long::compare).stream()
                .findAny()
                .orElseThrow() + 1;
        final ReservationRequest request = new ReservationRequest("PK", tomorrow, timeId, notExistThemeId);

        // when & then
        assertThatThrownBy(() -> reservationService.addReservation(request))
                .isInstanceOf(ThemeNotFoundException.class);
    }

    @Test
    @DisplayName("예약 하려는 일정이 오늘 1분 전일 경우 예외가 발생한다.")
    void validateReservationTimeAfterThanNow() {
        // given
        final String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        final String oneMinAgo = LocalTime.now().minusMinutes(1).format(DateTimeFormatter.ofPattern("HH:mm"));

        final ReservationTime time = reservationTimeRepository.save(new ReservationTime(null, oneMinAgo));
        final Long themeId = sampleThemes.get(0).getId();
        final ReservationRequest reservationRequest = new ReservationRequest("PK", today, time.getId(), themeId);

        // when & then
        assertThatThrownBy(() -> reservationService.addReservation(reservationRequest))
                .isInstanceOf(PreviousTimeException.class);
    }

    @Test
    @DisplayName("중복된 시간으로 예약을 할 때 예외가 발생한다.")
    void duplicateDateTimeReservation() {
        // given
        final ReservationRequest request = sampleReservations.get(0);
        reservationService.addReservation(request);


        // when & then
        assertThatThrownBy(() -> reservationService.addReservation(request))
                .isInstanceOf(ReservationDuplicatedException.class);
    }
}

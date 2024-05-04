package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReserveName;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@Import({H2ReservationRepository.class, H2ReservationTimeRepository.class, H2ThemeRepository.class})
@JdbcTest
class ReservationRepositoryTest {

    List<Reservation> sampleReservations = List.of(
            new Reservation(null,
                    new ReserveName("User 1"),
                    LocalDate.now().minusDays(1),
                    null,
                    null
            ),
            new Reservation(null,
                    new ReserveName("User 1"),
                    LocalDate.now(),
                    null,
                    null
            ),
            new Reservation(
                    null,
                    new ReserveName("User 2"),
                    LocalDate.now().plusDays(1),
                    null,
                    null
            )
    );
    List<ReservationTime> sampleTimes = List.of(
            new ReservationTime(null, "08:00"),
            new ReservationTime(null, "09:10")
    );
    List<Theme> sampleThemes = List.of(
            new Theme(null, "Theme 1", "Description 1", "Thumbnail 1"),
            new Theme(null, "Theme 2", "Description 2", "Thumbnail 2")
    );

    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    ReservationTimeRepository reservationTimeRepository;
    @Autowired
    ThemeRepository themeRepository;

    @BeforeEach
    void setUp() {
        Random random = new Random();

        sampleTimes = sampleTimes.stream()
                .map(reservationTimeRepository::save)
                .toList();
        sampleThemes = sampleThemes.stream()
                .map(themeRepository::save)
                .toList();
        sampleReservations = sampleReservations.stream()
                .map(reservation -> {
                    final ReservationTime randomTime = sampleTimes.get(random.nextInt(sampleTimes.size()));
                    final Theme randomTheme = sampleThemes.get(random.nextInt(sampleThemes.size()));

                    return reservation
                            .assignTime(randomTime)
                            .assignTheme(randomTheme);
                })
                .toList();
    }

    @Test
    @DisplayName("모든 예약 목록을 조회한다.")
    void findAll() {
        // given
        sampleReservations.forEach(reservationRepository::save);

        // when
        final var actual = reservationRepository.findAll();
        final List<Reservation> expected = IntStream.range(0, sampleReservations.size())
                .mapToObj(i -> sampleReservations.get(i).assignId(actual.get(i).getId()))
                .toList();

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("날짜와 테마 아이디로 모든 예약을 조회한다.")
    void findAllByDateAndThemeId() {
        // given
        final List<Reservation> savedReservations = sampleReservations.stream()
                .map(reservationRepository::save)
                .toList();
        final LocalDate date = savedReservations.get(0).getDate();
        final Long themeId = savedReservations.get(0).getTheme().getId();

        // when
        final List<Reservation> actual = reservationRepository.findAllByDateAndThemeId(date, themeId);
        final List<Reservation> expected = savedReservations.stream()
                .filter(reservation ->
                        reservation.getDate().equals(date) &&
                                reservation.getTheme().getId().equals(themeId))
                .toList();

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("특정 id를 통해 예약을 조회한다.")
    void findByIdPresent() {
        // given
        final Reservation reservation = sampleReservations.get(0);
        final Reservation savedReservation = reservationRepository.save(reservation);
        final Long savedId = savedReservation.getId();


        // when
        final Optional<Reservation> actual = reservationRepository.findById(savedId);
        final Reservation expected = reservation
                .assignId(savedId)
                .assignTime(new ReservationTime(reservation.getTime().getId()))
                .assignTheme(new Theme(reservation.getTheme().getId()));

        // then
        assertThat(actual).hasValue(expected);
    }

    @Test
    @DisplayName("존재하지 않는 예약을 조회할 경우 빈 값을 반환한다.")
    void findByIdNotExist() {
        // given
        final long notExistId = 1L;

        // when
        final Optional<Reservation> actual = reservationRepository.findById(notExistId);

        // then
        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("등록된 시간 아이디로 예약 존재 여부를 확인한다.")
    void existsByTimeIdPresent() {
        // given
        final Reservation reservation = sampleReservations.get(0);
        reservationRepository.save(reservation);
        final long existTimeId = reservation.getTime().getId();

        // when & then
        assertThat(reservationRepository.existsByTimeId(existTimeId)).isTrue();
    }

    @Test
    @DisplayName("등록되지 않은 시간 아이디로 예약 존재 여부를 확인한다.")
    void existsByTimeIdNotExist() {
        // given
        final long notExistTimeId = 0L;

        // when & then
        assertThat(reservationRepository.existsByTimeId(notExistTimeId)).isFalse();
    }

    @Test
    @DisplayName("등록된 테마 아이디로 예약 존재 여부를 확인한다.")
    void existsByThemeIdPresent() {
        // given
        final Reservation reservation = sampleReservations.get(0);
        reservationRepository.save(reservation);
        final long existThemeId = reservation.getTheme().getId();

        // when & then
        assertThat(reservationRepository.existsByThemeId(existThemeId)).isTrue();
    }

    @Test
    @DisplayName("등록되지 않은 테마 아이디로 예약 존재 여부를 확인한다.")
    void existsByThemeIdNotExist() {
        // given
        final long notExistThemeId = 0L;

        // when & then
        assertThat(reservationRepository.existsByThemeId(notExistThemeId)).isFalse();
    }

    @Test
    @DisplayName("예약 정보를 저장한다.")
    void save() {
        // given
        final Reservation reservation = sampleReservations.get(0);

        // when
        final Reservation actual = reservationRepository.save(reservation);
        final Reservation expected = reservation.assignId(actual.getId());

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간으로 예약을 저장하면 예외가 발생한다.")
    void exceptionOnSavingWithNotExistTime() {
        // given
        final long notExistTimeId = sampleTimes.stream()
                .map(ReservationTime::getId)
                .max(Long::compare)
                .orElse(0L) + 1;
        final Reservation reservation = sampleReservations.get(0)
                .assignTime(new ReservationTime(notExistTimeId));

        // when & then
        assertThatCode(() -> reservationRepository.save(reservation))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("존재하지 않는 테마 정보로 예약을 저장하면 예외가 발생한다.")
    void exceptionOnSavingWithNotExistTheme() {
        // given
        final long notExistTimeId = sampleThemes.stream()
                .map(Theme::getId)
                .max(Long::compare)
                .orElse(0L) + 1;
        final Reservation reservation = sampleReservations.get(0)
                .assignTheme(new Theme(notExistTimeId));

        // when & then
        assertThatCode(() -> reservationRepository.save(reservation))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("등록된 예약 번호로 삭제한다.")
    void deletePresent() {
        // given
        final Reservation reservation = sampleReservations.get(0);
        final Reservation savedReservation = reservationRepository.save(reservation);
        final Long existId = savedReservation.getId();

        // when & then
        assertThat(reservationRepository.findById(existId)).isPresent();
        assertThat(reservationRepository.delete(existId)).isNotZero();
        assertThat(reservationRepository.findById(existId)).isEmpty();
    }

    @Test
    @DisplayName("없는 예약 번호로 삭제할 경우 아무런 영향이 없다.")
    void deleteNotExist() {
        // given
        final long nonExistId = 1L;

        // when & then
        assertThat(reservationRepository.findById(nonExistId)).isEmpty();
        assertThat(reservationRepository.delete(nonExistId)).isZero();
    }
}

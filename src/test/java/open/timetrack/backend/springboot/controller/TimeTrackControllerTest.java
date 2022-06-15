package open.timetrack.backend.springboot.controller;

import java.time.OffsetDateTime;
import java.util.List;

import javax.validation.ConstraintViolationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import open.timetrack.api.springcloud.payload.TimeTrack;
import open.timetrack.backend.springboot.testutil.comparator.TimeTrackIdFieldIgnoringComparator;

@SpringBootTest
class TimeTrackControllerTest {

    private final TimeTrackIdFieldIgnoringComparator idFieldIgnoringComparator
            = new TimeTrackIdFieldIgnoringComparator();
    @Autowired
    TimeTrackController timeTrackController;
    private TimeTrack existingTimeTrack;

    @BeforeEach
    void setUp() {
        existingTimeTrack = timeTrackController.getTimeTrack("test_task").getBody();

        if (existingTimeTrack == null) {
            existingTimeTrack = new TimeTrack()
                    .id("test_task")
                    .task("Test task")
                    .start(OffsetDateTime.now());
            timeTrackController.addNewTimeTrack(existingTimeTrack);
        }
    }

    @Test
    void addNewTimeTrack_whenAddingInvalidTimeTrack_shouldThrowValidationError() {
        assertThatCode(() -> timeTrackController.addNewTimeTrack(new TimeTrack()))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("addNewTimeTrack.arg0.start:")
                .hasMessageContaining("addNewTimeTrack.arg0.task:");
    }

    @Test
    void addNewTimeTrack_whenGivenNoId_shouldGenerateUniqueUUID() {
        final ResponseEntity<List<TimeTrack>> allTimeTracksBeforeRequest = timeTrackController.getAllTimeTracks();

        final TimeTrack expected = new TimeTrack().task("myTest").start(OffsetDateTime.now());
        timeTrackController.addNewTimeTrack(expected);

        final ResponseEntity<List<TimeTrack>> allTimeTracksAfterRequest = timeTrackController.getAllTimeTracks();

        final List<TimeTrack> allTimeTracksAfter = allTimeTracksAfterRequest.getBody();
        allTimeTracksAfter.removeAll(allTimeTracksBeforeRequest.getBody());

        assertThat(allTimeTracksAfter).hasSize(1);
        final TimeTrack actual = allTimeTracksAfter.get(0);
        assertThat(actual).usingComparator(idFieldIgnoringComparator).isEqualTo(expected);
        assertThat(actual.getId()).isNotBlank();
    }

    @Test
    void addNewTimeTrack_whenAddingTheSameIdAgain_shouldGenerateNewUUID() {
        final ResponseEntity<List<TimeTrack>> allTimeTracksBeforeRequest = timeTrackController.getAllTimeTracks();

        final TimeTrack cloneOfExisting = new TimeTrack().id(existingTimeTrack.getId())
                                                         .task(existingTimeTrack.getTask())
                                                         .start(existingTimeTrack.getStart())
                                                         .end(existingTimeTrack.getEnd())
                                                         .note(existingTimeTrack.getNote());
        assertThat(timeTrackController.addNewTimeTrack(cloneOfExisting))
                .matches(response -> response.getStatusCode().is2xxSuccessful());

        final ResponseEntity<List<TimeTrack>> allTimeTracksAfterRequest = timeTrackController.getAllTimeTracks();
        final List<TimeTrack> allTimeTracksAfter = allTimeTracksAfterRequest.getBody();

        assertThat(allTimeTracksAfter).contains(existingTimeTrack, cloneOfExisting);

        allTimeTracksAfter.removeAll(allTimeTracksBeforeRequest.getBody());

        assertThat(allTimeTracksAfter).hasSize(1);
        assertThat(allTimeTracksAfter.get(0)).usingComparator(idFieldIgnoringComparator).isEqualTo(existingTimeTrack);
        assertThat(allTimeTracksAfter.get(0)).isNotEqualTo(existingTimeTrack);
    }

    @Test
    void getAllTimeTracks_whenAskingForMyList_shouldReturnMyList() {
        final ResponseEntity<List<TimeTrack>> response = timeTrackController.getAllTimeTracks();

        assertThat(response).matches(r -> r.getStatusCode().is2xxSuccessful());
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void getTimeTrack_whenAskingForSpecificId_shouldGiveThatEntry() {
        final ResponseEntity<TimeTrack> response = timeTrackController.getTimeTrack("test_task");

        assertThat(response).matches(r -> r.getStatusCode().is2xxSuccessful());
        assertThat(response.getBody()).isEqualTo(existingTimeTrack);
    }

    @Test
    void getTimeTrack_whenAskingForNotExistingId_shouldReturn404() {
        final ResponseEntity<TimeTrack> response = timeTrackController.getTimeTrack("not_existing_time_track");

        assertThat(response).matches(r -> r.getStatusCode() == HttpStatus.NOT_FOUND);
    }

    @Test
    void updateTimeTrack_whenAFieldIsUpdated_shouldStoreAndReturnNewValue() {
        final TimeTrack updatedTimeTrack = new TimeTrack()
                .id(existingTimeTrack.getId())
                .start(existingTimeTrack.getStart())
                .task(existingTimeTrack.getTask())
                .note("new Note");
        final ResponseEntity<Void> updateResponse = timeTrackController.updateTimeTrack("test_task", updatedTimeTrack);

        assertThat(updateResponse).matches(r -> r.getStatusCode().is2xxSuccessful());

        final ResponseEntity<TimeTrack> getResponse = timeTrackController.getTimeTrack("test_task");

        assertThat(getResponse).matches(r -> r.getStatusCode().is2xxSuccessful());
        assertThat(getResponse.getBody()).isEqualTo(updatedTimeTrack);
    }

    @Test
    void updateTimeTrack_whenUnknownTimeTrackShouldBeUpdated_shouldReturnBadRequest() {
        final ResponseEntity<Void> response1 = timeTrackController.updateTimeTrack("unknown_task", existingTimeTrack);

        assertThat(response1).matches(r -> r.getStatusCode() == HttpStatus.BAD_REQUEST);
    }
}

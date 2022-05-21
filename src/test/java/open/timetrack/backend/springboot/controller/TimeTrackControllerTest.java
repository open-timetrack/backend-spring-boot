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

@SpringBootTest
class TimeTrackControllerTest {

    @Autowired
    TimeTrackController timeTrackController;
    private TimeTrack existingTimeTrack;

    @BeforeEach
    void setUp() {
        existingTimeTrack = new TimeTrack()
                .id("test_task")
                .task("Test task")
                .start(OffsetDateTime.now());
        timeTrackController.addNewTimeTrack(existingTimeTrack);
    }

    @Test
    void addNewTimeTrack_whenAddingInvalidTimeTrack_shouldThrowValidationError() {
        assertThatCode(() -> timeTrackController.addNewTimeTrack(new TimeTrack()))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("addNewTimeTrack.arg0.start:")
                .hasMessageContaining("addNewTimeTrack.arg0.task:");
    }

    @Test
    void getAllTimeTracks_whenAskingForMyList_shouldReturnMyList() {
        final ResponseEntity<List<TimeTrack>> response = timeTrackController.getAllTimeTracks();

        assertThat(response)
                .matches(r -> r.getStatusCode().is2xxSuccessful());
        assertThat(response.getBody())
                .hasSize(1);
    }

    @Test
    void getTimeTrack_whenAskingForSpecificId_shouldGiveThatEntry() {
        final ResponseEntity<TimeTrack> response = timeTrackController.getTimeTrack("test_task");

        assertThat(response)
                .matches(r -> r.getStatusCode().is2xxSuccessful());
        assertThat(response.getBody())
                .matches(timeTrack -> timeTrack.getId().equals("test_task"))
                .matches(timeTrack -> timeTrack.getTask().equals("Test task"));
    }

    @Test
    void getTimeTrack_whenAskingForNotExistingId_shouldReturn404() {
        final ResponseEntity<TimeTrack> response = timeTrackController.getTimeTrack("not_existing_time_track");

        assertThat(response)
                .matches(r -> r.getStatusCode() == HttpStatus.NOT_FOUND);
    }

    @Test
    void updateTimeTrack_whenAFieldIsUpdated_shouldStoreAndReturnNewValue() {
        final TimeTrack updatedTimeTrack = new TimeTrack()
                .id(existingTimeTrack.getId())
                .start(existingTimeTrack.getStart())
                .task(existingTimeTrack.getTask())
                .note("new Note");
        final ResponseEntity<Void> response1 = timeTrackController.updateTimeTrack("test_task", updatedTimeTrack);

        assertThat(response1)
                .matches(r -> r.getStatusCode().is2xxSuccessful());

        final ResponseEntity<TimeTrack> response2 = timeTrackController.getTimeTrack("test_task");

        assertThat(response2)
                .matches(r -> r.getStatusCode().is2xxSuccessful());
        assertThat(response2.getBody())
                .matches(timeTrack -> timeTrack.getId().equals("test_task"))
                .matches(timeTrack -> timeTrack.getTask().equals("Test task"))
                .matches(timeTrack -> timeTrack.getNote().equals("new Note"));
    }

    @Test
    void updateTimeTrack_whenUnknownTimeTrackShouldBeUpdated_shouldReturnBadRequest() {
        final ResponseEntity<Void> response1 = timeTrackController.updateTimeTrack("unknown_task", existingTimeTrack);

        assertThat(response1)
                .matches(r -> r.getStatusCode() == HttpStatus.BAD_REQUEST);
    }
}

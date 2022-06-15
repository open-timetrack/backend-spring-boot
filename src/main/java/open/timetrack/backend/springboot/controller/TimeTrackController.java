package open.timetrack.backend.springboot.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import open.timetrack.api.springcloud.TimeTrackApi;
import open.timetrack.api.springcloud.payload.TimeTrack;

@Controller
public class TimeTrackController implements TimeTrackApi {
    private Map<String, TimeTrack> timeTracks = new HashMap<>();

    public ResponseEntity<Void> addNewTimeTrack(@Valid final TimeTrack timeTrack) {
        while (StringUtils.isBlank(timeTrack.getId()) || timeTracks.containsKey(timeTrack.getId())) {
            timeTrack.setId(UUID.randomUUID().toString());
        }
        timeTracks.put(timeTrack.getId(), timeTrack);
        return ResponseEntity.ok(null);
    }

    public ResponseEntity<List<TimeTrack>> getAllTimeTracks() {

        return ResponseEntity.ok(new ArrayList<>(timeTracks.values()));
    }

    public ResponseEntity<TimeTrack> getTimeTrack(final String s) {
        return Optional.ofNullable(timeTracks.get(s))
                       .map(ResponseEntity::ok)
                       .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public ResponseEntity<Void> updateTimeTrack(final String s, @Valid final TimeTrack timeTrack) {
        if (timeTracks.containsKey(s)) {
            timeTracks.put(s, timeTrack);
            return ResponseEntity.ok(null);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}

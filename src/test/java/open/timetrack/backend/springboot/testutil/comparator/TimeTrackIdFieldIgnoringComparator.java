package open.timetrack.backend.springboot.testutil.comparator;

import java.util.Comparator;
import java.util.Objects;

import open.timetrack.api.springcloud.payload.TimeTrack;

public class TimeTrackIdFieldIgnoringComparator implements Comparator<TimeTrack> {
    @Override
    public int compare(final TimeTrack first, final TimeTrack second) {
        return Objects.equals(first.getStart(), second.getStart()) &&
               Objects.equals(first.getEnd(), second.getEnd()) &&
               Objects.equals(first.getTask(), second.getTask()) &&
               Objects.equals(first.getNote(), second.getNote())
               ? 0 : 1;
    }
}

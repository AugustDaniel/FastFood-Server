import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class Lap implements Serializable, Comparable<LocalTime> {

    private String name;
    private LocalTime lapTime;
    private LocalDate dateOfDrive;

    public Lap(String name, LocalTime lapTime, LocalDate dateOfDrive) {
        this.name = name;
        this.lapTime = lapTime;
        this.dateOfDrive = dateOfDrive;
    }

    public String getName() {
        return name;
    }

    public LocalTime getLapTime() {
        return lapTime;
    }

    public LocalDate getDateOfDrive() {
        return dateOfDrive;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLapTime(LocalTime lapTime) {
        this.lapTime = lapTime;
    }

    public void setDateOfDrive(LocalDate dateOfDrive) {
        this.dateOfDrive = dateOfDrive;
    }

    @Override
    public int compareTo(LocalTime o) {
        return this.lapTime.compareTo(o);
    }
}

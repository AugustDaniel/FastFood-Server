import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class RaceTracker {

    public AtomicBoolean inProgress = new AtomicBoolean(false);
    private Timer timer = new Timer();

    public void start() {
        if (inProgress.get()) {
            return;
        }

        inProgress.set(true);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (inProgress.get()) {
                    Race.endRace();
                    end();
                }
            }
        }, 180000);
    }

    public void end() {
        inProgress.set(false);
    }
}

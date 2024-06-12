import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class RaceTracker {

    public AtomicBoolean inProgress = new AtomicBoolean(false);
    private volatile Timer timer = new Timer();

    public void start() {
        if (inProgress.get()) {
            return;
        }

        inProgress.set(true);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (inProgress.get()) {
                    Race.endRacePrematurely();
                    inProgress.set(false);
                }
            }
        }, 180000);
    }

    public void end() {
        inProgress.set(false);
        timer.cancel();
    }
}

import com.fastfoodlib.util.Lap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

public class Race {

    private static final int AMOUNT_OF_PLAYERS = 1;
    private static final int AMOUNT_OF_LAPS = 1;
    private static final RaceTracker tracker = new RaceTracker();
    private static final ArrayBlockingQueue<Connection> connections = new ArrayBlockingQueue<>(AMOUNT_OF_PLAYERS);
    private static final ConcurrentLinkedQueue<Lap> allLaps = new ConcurrentLinkedQueue<>();
    private static CountDownLatch waiter = new CountDownLatch(AMOUNT_OF_PLAYERS);

    public static void join(Connection connection) throws Exception {
        connections.put(connection);

        while (true) {
            waiter.countDown();
            waiter.await();

            try {
                connection.checkStart();
            } catch (Exception e) {
                connections.remove(connection);
                waiter = new CountDownLatch(AMOUNT_OF_PLAYERS);
                throw e;
            }

            if (waiter.getCount() == 0) {
                connection.sendStart();
                break;
            }

            connection.sendWait();
        }

        tracker.start();
        List<Lap> laps = new ArrayList<>();

        for (int i = 0; i < AMOUNT_OF_LAPS; i++) {
            Lap lap = connection.getLapTime();
            laps.add(lap);
            Server.printLog("lap received: " + lap.getLapTimeFormatted());
        }


        addLaps(connection, laps);
    }

    private static void addLaps(Connection connection, List<Lap> laps) throws Exception {
        Server.printLog("adding laps " + laps);
        Collections.sort(laps);
        allLaps.add(laps.get(0));
        connection.sendTimeout(); // end race for them

        if (allLaps.size() == AMOUNT_OF_PLAYERS) {
            endRace();
        }
    }

    public static void endRace() {
        Server.printLog("sending results");
        connections.forEach(c -> {
            c.sendResult(new ArrayList<>(allLaps));
            c.disconnect();
        });
        Server.addToLeaderboard(new ArrayList<>(allLaps));
        allLaps.clear();
        connections.clear();
        tracker.end();
    }

    public static void endRacePre() {
        connections.forEach(c -> {
            try {
                if (c.racing.get()) {
                    c.sendTimeout();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        endRace();
    }
}

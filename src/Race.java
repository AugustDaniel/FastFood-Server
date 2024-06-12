import com.fastfoodlib.util.Lap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

public class Race {

    private static final int amountOfPlayers = 1;
    private static final int amountOfLaps = 1;
    private static final RaceTracker tracker = new RaceTracker();
    private static final ArrayBlockingQueue<Connection> connections = new ArrayBlockingQueue<>(amountOfPlayers);
    private static final ConcurrentLinkedQueue<Lap> allLaps = new ConcurrentLinkedQueue<>();
    private static CountDownLatch waiter = new CountDownLatch(amountOfPlayers);

    public static void join(Connection connection) throws Exception {
        Server.printLog("players waiting for race: " + connection.toString());
        Server.printLog("in connection list: " + connections.toString());
        // blocking call until connections list has space
        connections.put(connection);
        Server.printLog("player added to waiting list + " + connection.toString());

        while (true) {
            // wait for enough players
            waiter.countDown();
            waiter.await();

            try {
                connection.checkStart();
            } catch (Exception e) {
                // remove connection in case of error
                connections.remove(connection);
                waiter = new CountDownLatch(amountOfPlayers);
                throw e;
            }

            if (waiter.getCount() == 0) {
                // end of loop and signal client to start race
                connection.sendStart();
                break;
            }

            // signal client to wait
            connection.sendWait();
        }

        tracker.start();
        List<Lap> laps = new ArrayList<>();

        for (int i = 0; i < amountOfLaps; i++) {
            Lap lap = connection.getLapTime();
            laps.add(lap);
            Server.printLog("lap received: " + lap.getLapTimeFormatted());
        }

        ;
        addLaps(connection, laps);
    }

    private static void addLaps(Connection connection, List<Lap> laps) throws Exception {
        Server.printLog("adding laps " + laps);
        Collections.sort(laps);
        allLaps.add(laps.get(0));
        connection.sendTimeout(); // signal end of race for client

        if (allLaps.size() == amountOfPlayers) {
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

    public static void endRacePrematurely() {
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

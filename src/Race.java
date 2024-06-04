import com.fastfoodlib.util.Lap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class Race {

    public static final int AMOUNT_OF_PLAYERS = 1;
    public static final int AMOUNT_OF_LAPS = 3;
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
            laps.add(connection.getLapTime());
        }

        Server.printLog("sending laps");
        addLaps(laps);
    }

    private static void addLaps(List<Lap> laps) {
        allLaps.addAll(laps);

        if (allLaps.size() == AMOUNT_OF_PLAYERS * AMOUNT_OF_LAPS) {
            endRace();
        }
    }

    public static void endRace() {
        Server.printLog("sending results");
        connections.forEach(c -> c.sendResult(new ArrayList<>(allLaps)));
        Server.addToLeaderboard(new ArrayList<>(allLaps));
        allLaps.clear();
        connections.clear();
        tracker.end();
    }
}

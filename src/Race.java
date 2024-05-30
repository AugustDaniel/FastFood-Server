import com.fastfoodlib.util.Lap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

public class Race {

    public static final int AMOUNT_OF_PLAYERS = 1;
    public static final int AMOUNT_OF_LAPS = 1;
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

        RaceTracker.raceTracker.start();
        List<Lap> laps = new ArrayList<>();

        for (int i = 0; i < AMOUNT_OF_LAPS; i++) {
            laps.add(connection.getLapTime());
        }

        System.out.println(laps);
        System.out.println("going to add laps");

        addLaps(laps);
    }

    private static void addLaps(List<Lap> laps) {
        allLaps.addAll(laps);

        if (allLaps.size() == AMOUNT_OF_PLAYERS * AMOUNT_OF_LAPS) {
            endRace();
        }
    }

    public static void endRace() {
        System.out.println("sending laps");
        connections.forEach(c -> c.sendResult(new ArrayList<>()));
        Set<Lap> serverleaderboard = Server.getLeaderboard();
        serverleaderboard.addAll(allLaps);
        allLaps.clear();
        connections.clear();
        RaceTracker.raceTracker.end();
    }
}

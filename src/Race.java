import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

public class Race {

    public static final int AMOUNT_OF_PLAYERS = 1;
    public static final int AMOUNT_OF_LAPS = 1;
    private static final ArrayBlockingQueue<Connection> connections = new ArrayBlockingQueue<>(AMOUNT_OF_PLAYERS);
    private static final ConcurrentLinkedQueue<Lap> allLaps = new ConcurrentLinkedQueue<>();
    private static CountDownLatch waiter = new CountDownLatch(AMOUNT_OF_PLAYERS);

    public static void join(Connection connection) {
        try {
            connections.put(connection);
            System.out.println("waiting");
            waiter.countDown();
            waiter.await();
            waiter = new CountDownLatch(AMOUNT_OF_PLAYERS);
            System.out.println("done waiting");
        } catch (Exception e) {
            e.printStackTrace();
        }


        connection.sendStart();

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
            System.out.println("sending laps");
            connections.forEach(c -> c.sendResult(new ArrayList<>(laps)));
            Server.getLeaderboard().addAll(allLaps);
            allLaps.clear();
            connections.clear();
        }
    }
}

import java.net.ServerSocket;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static ServerSocket serverSocket;
    private static Set<Lap> leaderboard = new TreeSet<>();

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(8000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ExecutorService service = Executors.newCachedThreadPool();

        while (true) {
            try {
                service.execute(new Connection(serverSocket.accept()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Set<Lap> getLeaderboard() {
        return leaderboard;
    }
}
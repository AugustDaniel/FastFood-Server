import java.net.ServerSocket;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static ServerSocket serverSocket;
    private static Set<Map.Entry<String, LocalTime>> leaderboard = new LinkedHashSet<>();

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

    public static Set<Map.Entry<String, LocalTime>> getLeaderboard() {
        return leaderboard;
    }
}
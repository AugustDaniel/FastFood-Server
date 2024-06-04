import com.fastfoodlib.util.Lap;

import java.io.*;
import java.net.ServerSocket;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static ServerSocket serverSocket;
    private static List<Lap> leaderboard = null;
    private static final String PATH_LEADERBOARD = "leaderboard.dat";

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(8000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (FileInputStream fileInputStream = new FileInputStream(PATH_LEADERBOARD);
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
            leaderboard = (List<Lap>) objectInputStream.readObject();
        } catch (Exception ex) {
            leaderboard = new ArrayList<>();
            ex.getMessage();
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

    public static List<Lap> getLeaderboard() {
        return new ArrayList<>(leaderboard);
    }

    public static synchronized void addToLeaderboard(List<Lap> laps) {
        leaderboard.addAll(laps);

        try (FileOutputStream fileOutputStream = new FileOutputStream(PATH_LEADERBOARD);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
            objectOutputStream.writeObject(leaderboard);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void printLog(String message) {
        System.out.println(LocalTime.now() + ": " + message);
    }
}
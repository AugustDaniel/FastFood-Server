import com.fastfoodlib.util.Lap;

import java.net.ServerSocket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static ServerSocket serverSocket;
    private static List<Lap> leaderboard = new ArrayList<>();

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(8000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ExecutorService service = Executors.newCachedThreadPool();
        leaderboard.add(new Lap("Pieter", LocalTime.now(), LocalDate.now()));
        leaderboard.add(new Lap("Jayson", LocalTime.now(), LocalDate.now()));
        leaderboard.add(new Lap("Joshua", LocalTime.now(), LocalDate.now()));
        leaderboard.add(new Lap("Tim", LocalTime.now(), LocalDate.now()));
        leaderboard.add(new Lap("Luuk", LocalTime.now(), LocalDate.now()));
        leaderboard.add(new Lap("Erik", LocalTime.now(), LocalDate.now()));

        while (true) {
            try {
                service.execute(new Connection(serverSocket.accept()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static List<Lap> getLeaderboard() {
        return leaderboard;
    }
}
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class Connection implements Runnable {

    private Socket client;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    public Connection(Socket client) {
        this.client = client;

        try {
            this.input = new ObjectInputStream(client.getInputStream());
            this.output = new ObjectOutputStream(client.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (this.client.isConnected()) {
            try {
                byte option = input.readByte();
                System.out.println("got option" + option);
                switch (option) {
                    case 0:
                        startRace();
                        break;
                    case 1:
                        sendLeaderBoard();
                        break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void startRace() {
        System.out.println("race started");
        Race.join(this);
    }

    private void sendLeaderBoard() {
        try {
            output.writeObject(Server.getLeaderboard());
            output.flush();
            output.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map.Entry<String, LocalTime> getLapTime() {
        Map.Entry<String, LocalTime> lap = null;
        try {
            lap = (Map.Entry<String, LocalTime>) input.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lap;
    }

    public void sendStart() {
        System.out.println("sending start");
        try {
            output.writeBoolean(true);
            output.flush();
            output.reset();
            System.out.println("sent start");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendResult(List<Map.Entry<String, LocalTime>> laps) {
        try {
            output.writeObject(laps);
            output.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

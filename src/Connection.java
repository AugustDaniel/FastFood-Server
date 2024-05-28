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

                switch (option) {
                    case 0:
                        sendLeanderBoard();
                        break;
                    case 1:
                        startRace();
                        break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void startRace() {
        Race.join(this);
    }

    private void sendLeanderBoard() {
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
        try {
            output.writeBoolean(true);
            output.flush();
            output.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendResult(List<Map.Entry<String, LocalTime>> laps) {
        try {
            output.writeObject(laps);
            output.flush();
            output.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

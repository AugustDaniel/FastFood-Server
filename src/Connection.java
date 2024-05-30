import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import com.fastfoodlib.util.*;

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
        while (true) {
            try {
                Options option = (Options) input.readObject();
                System.out.println("got option" + option);
                switch (option) {
                    case JOIN_RACE:
                        joinRace();
                        break;
                    case REQUEST_LEADERBOARD:
                        sendLeaderBoard();
                        break;
                    default:
                        output.writeBoolean(false);
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void joinRace() throws Exception {
        System.out.println("race joined");
        Race.join(this);
    }

    private void sendLeaderBoard() throws Exception {
        output.writeObject(Server.getLeaderboard());
        output.flush();
        output.reset();
    }

    public Lap getLapTime() {
        Lap lap = null;
        try {
            lap = (Lap) input.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lap;
    }

    public void sendStart() throws Exception {
        output.writeBoolean(true);
        output.flush();
        output.reset();
        System.out.println("sent start");
    }

    public void checkStart() throws Exception {
        output.writeObject(Options.START_RACE);
        output.flush();

        Options option = (Options) input.readObject();
        if (option != Options.START_RACE) {
            throw new EOFException();
        }
    }

    public void sendResult(List<Lap> laps) {
        try {
            output.writeObject(laps);
            output.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

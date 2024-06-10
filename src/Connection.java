import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fastfoodlib.util.*;

public class Connection implements Runnable {

    private Socket client;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    public AtomicBoolean racing = new AtomicBoolean(false);

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
                Server.printLog("got option " + option.toString());
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
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnect() {
        try {
            if (client != null) client.close();
            if (input != null) input.close();
            if (output != null) output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void joinRace() throws Exception {
        Server.printLog("joined race");
        Race.join(this);
    }

    private void sendLeaderBoard() throws Exception {
        Server.printLog(Server.getLeaderboard().toString());
        output.writeObject(Server.getLeaderboard());
        output.flush();
        output.reset();
    }

    public Lap getLapTime() throws IOException, ClassNotFoundException {
        return (Lap) input.readObject();
    }

    public void sendStart() throws Exception {
        output.writeBoolean(true);
        output.flush();
        output.reset();
        racing.set(true);
        Server.printLog("send start");
    }

    public void sendWait() throws Exception {
        output.writeBoolean(false);
        output.flush();
        output.reset();
        Server.printLog("send wait");
    }

    public void checkStart() throws Exception {
        Server.printLog("checking start");
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

    public void sendTimeout() throws IOException {
        output.writeBoolean(true);
        output.flush();
        output.reset();
        racing.set(false);
        Server.printLog("sent timeout");
    }
}

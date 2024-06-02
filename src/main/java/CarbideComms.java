import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class CarbideComms {
    private static volatile CarbideComms instance = null;
    private CarbideState state;

    private enum CarbideState {
        INITIALIZING,
        SERVICE,
        INFIELDUPDATE,
        STANDINGBY,
        GOINGTOSTANDBY,
        FAILURE,
        HOUSEKEEPING,
        OPERATIONAL
    }
    private final String ipAddress;
    private final String port = "20018";
    private int preset = 0;
    private Thread thread;
    private ArrayList<Command> queue;
    private boolean terminated;
    private boolean connected;

    private CarbideComms(String ipAddress) {
        this.ipAddress = ipAddress;
        this.queue = new ArrayList<>();
        this.thread = new Thread(this::commLoop, "CarbideComms");
        thread.start();
    }

    public static CarbideComms startComms(String ipAddress) {
        synchronized (CarbideComms.class){
            if (instance == null) {
                instance = new CarbideComms(ipAddress);
            }
        }
        return instance;
    }

    public void stopComms() {
        synchronized (this) {
            if (instance != null) {
                runShutdownSequence();
                instance = null;
            }
        }
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getPort() {
        return port;
    }

    public boolean getConnected() {
        return connected;
    }

    private void commLoop() {
        try {
            connected = true;
            terminated = false;
            System.out.println("Connected to Carbide Laser");
//            runStartupSequence(); // TODO - Put startup into queue
            while (!terminated) {
                synchronized (queue){
                    for (Command command : queue) {
                        System.out.println(command.send());
                        Thread.sleep(1000);
                    }
                    queue.clear();
                }
            }
        } catch (InterruptedException | IOException e) {
            System.out.println("Ending Carbide Communications");
            terminated = true;
            stopComms();
        }
    }

    private void runStartupSequence() {
       selectPreset(preset);
       applySelectedPreset();
       while (!state.equals(CarbideState.OPERATIONAL)) {
//           getCurrentState();
       }
    }

    private void runShutdownSequence() {
        // TODO - perform shutdown sequence
        if (thread.isAlive()) {
            // TODO - shutdown thread
        }
    }

    public void selectPreset(int index) {
        synchronized (queue) {
            queue.add(new Command(this, "Basic/SelectedPresetIndex", index));
        }
    }

    public void applySelectedPreset() {
        synchronized (queue) {
            queue.add(new Command(this, "Basic/ApplySelectedPreset"));
        }
    }

    public void requestState() {
        synchronized (queue) {
            queue.add(new Command(this, "Basic/ActualStateName"));
        }
    }

}

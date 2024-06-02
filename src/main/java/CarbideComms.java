import java.io.IOException;
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
    private final String preset = "0";
    private final Thread commandThread;
    private final ArrayList<Request> queue;
    private boolean terminated;
    private boolean connected;

    private CarbideComms(String ipAddress) {
        this.ipAddress = ipAddress;
        this.queue = new ArrayList<>();
        this.commandThread = new Thread(this::commLoop, "command_loop");
        commandThread.start();
    }

    public static CarbideComms create(String ipAddress) {
        synchronized (CarbideComms.class){
            if (instance == null) {
                instance = new CarbideComms(ipAddress);
            }
        }
        return instance;
    }

    public void destroy() {
        synchronized (this) {
            if (instance != null) {
                runShutdownSequence();
                instance = null;
            }
        }
    }

    private void commLoop() {
        try {
            terminated = false;
            connected = true;
            state = setState();
            System.out.println("Connected to Carbide Laser");

            queueStartupRequests();
            System.out.println("Entering startup phase...");
            while (!terminated) {
                synchronized (queue){
                    for (Request request : queue) {
                        var response = request.send();
                        System.out.println("Response: " + response);
                        Thread.sleep(1000);
                    }
                    queue.clear();
                    checkOperational();
                }
            }
        } catch (InterruptedException | IOException e) {
            System.out.println("Carbide communications dropped");
            terminated = true;
            destroy();
        }
    }

    private void queueStartupRequests() {
       selectPresetIndex(preset);
       applySelectedPreset();
    }

    private void checkOperational() throws InterruptedException, IOException {
        while(!state.equals(CarbideState.OPERATIONAL)) {
            System.out.println("State check: " + state);
            Thread.sleep(1000);
            state = setState();
        }
        queue.clear();
    }

    private CarbideState setState() throws IOException, InterruptedException {
        return CarbideState.valueOf(requestState().send().toUpperCase().replace("\"", ""));
    }

    private void runShutdownSequence() {
        // TODO - perform shutdown sequence
        if (commandThread.isAlive()) {
            // kill thread
        }
    }

    public Request selectPresetIndex(String index) {
        Request request = new Request(this, Request.Method.PUT, "Basic/SelectedPresetIndex", index);
        synchronized (queue) {
            queue.add(request);
        }
        return request;
    }

    public Request applySelectedPreset() {
        Request request = new Request(this, Request.Method.POST, "Basic/ApplySelectedPreset", "");
        synchronized (queue) {
            queue.add(request);
        }
        return request;
    }

    private Request requestState() {
        Request request = new Request(this, Request.Method.GET, "Basic/ActualStateName", "");
        synchronized (queue) {
            queue.add(request);
        }
        return request;
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
}

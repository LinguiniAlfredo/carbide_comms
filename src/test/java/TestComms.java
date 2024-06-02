import java.net.URISyntaxException;

public class TestComms {
    public static void main (String[] args) throws URISyntaxException {
        CarbideComms comms = CarbideComms.startComms( "127.0.0.1");
        comms.selectPreset(0);
        comms.applySelectedPreset();
        comms.requestState();
        comms.requestState();
        comms.requestState();
    }
}

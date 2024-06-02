import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class Command {
    private HttpRequest request;
    private String uri;
    protected enum requestType { GET, PUT, POST }

//    public Command (CarbideComms comms, String endpoint, requestType method){
//        switch (method){
//            case GET -> new Command(comms, endpoint);
//            case PUT -> new Command(comms, endpoint);
//            case POST -> new Command(comms, endpoint);
//        }
//    }
    public Command (CarbideComms comms, String endpoint) {
        this.uri = "http://" + comms.getIpAddress() + ":" + comms.getPort() + "/v1/" + endpoint;
        this.request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET()
                .build();
    }

    public Command (CarbideComms comms, String endpoint, int index) {
        this.uri = "http://" + comms.getIpAddress() + ":" + comms.getPort() + "/v1/" + endpoint;
        this.request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .headers("Content-Type", "text/plain;charset=UTF-8")
                .PUT(HttpRequest.BodyPublishers.ofString(Integer.toString(index)))
                .build();
    }

    public String send() throws IOException, InterruptedException {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            System.out.println("Sending request: " + uri);
            return client.send(request, HttpResponse.BodyHandlers.ofString()).body();

        } catch (IOException e) {
            System.out.println("error in sending request");
        }
        return null;
    }
}

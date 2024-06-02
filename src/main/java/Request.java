import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class Request {
    private final HttpRequest request;
    private final String uri;
    protected enum Method { GET, PUT, POST }

    public Request(CarbideComms comms, Method method, String endpoint, String args){
        switch (method){
            case GET, default -> {
                this.uri = "http://" + comms.getIpAddress() + ":" + comms.getPort() + "/v1/" + endpoint;
                this.request = HttpRequest.newBuilder()
                        .uri(URI.create(uri))
                        .GET()
                        .build();
            }
            case PUT -> {
                this.uri = "http://" + comms.getIpAddress() + ":" + comms.getPort() + "/v1/" + endpoint;
                this.request = HttpRequest.newBuilder()
                        .uri(URI.create(uri))
                        .PUT(HttpRequest.BodyPublishers.ofString(args))
                        .build();
            }
            case POST -> {
                this.uri = "http://" + comms.getIpAddress() + ":" + comms.getPort() + "/v1/" + endpoint;
                this.request = HttpRequest.newBuilder()
                        .uri(URI.create(uri))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();
            }
        }
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

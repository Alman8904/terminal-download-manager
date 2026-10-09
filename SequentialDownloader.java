import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class SequentialDownloader {

    private final HttpClient client;

    public SequentialDownloader() {
        client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public void download(String link, Path destination) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(link))
                .GET()
                .build();

        HttpResponse<InputStream> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofInputStream()
        );

        if (response.statusCode() != 200) {
            response.body().close();
            throw new RuntimeException(
                    "Download failed, status " + response.statusCode()
            );
        }

        try (InputStream input = response.body()) {
            Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        System.out.println("Sequential download complete: " + destination);
    }
}
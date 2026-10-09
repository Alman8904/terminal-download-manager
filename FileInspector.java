
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FileInspector {

    private final HttpClient client;

    public FileInspector() {
        client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public long getFileSize(String link) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(link))
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<Void> response = client.send(
                request,
                HttpResponse.BodyHandlers.discarding()
        );

        return response.headers()
                .firstValueAsLong("Content-Length")
                .orElse(-1);
    }

    public boolean supportsRanges(String link) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(link))
                .header("Range", "bytes=0-0")
                .GET()
                .build();

        HttpResponse<InputStream> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofInputStream()
        );

        try (InputStream input = response.body()) {
            if (response.statusCode() != 206) {
                return false;
            }

            String contentRange = response.headers()
                    .firstValue("Content-Range")
                    .orElse("");

            return contentRange.matches("bytes 0-0/\\d+");
        }
    }
}

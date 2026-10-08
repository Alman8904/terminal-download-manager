import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Downloader {

    private final HttpClient client;
    private static final int PART_COUNT = 8;

    public Downloader() {

        //client which goes to server
        client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public void download(String link) throws Exception {

        long fileSize = getFileSize(link);
        System.out.println("File size: " + fileSize);

        long partSize = (fileSize + PART_COUNT - 1) / PART_COUNT;
        System.out.println("Part size: " + partSize);

        for (long start = 0; start < fileSize; start += partSize) {

            long end = Math.min(start + partSize - 1, fileSize - 1);

            System.out.println("Downloading: " + start + "-" + end);

            //request is needed by the client to give to the server
            HttpRequest request = HttpRequest.newBuilder(URI.create(link))
                    //giving instructions to the server
                    .header("Range", "bytes=" + start + "-" + end)
                    .build();

            //response is what the server gives back to the client
            HttpResponse<InputStream> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofInputStream()
            );

            if(response.statusCode() == 206) {
                System.out.println("Partial download successful");

                long size = response.headers()
                        .firstValueAsLong("Content-Length")
                        .orElse(-1);

                String ranges = response.headers()
                        .firstValue("Accept-Ranges")
                        .orElse("none");

                System.out.println("Content-Length: " + size);
                System.out.println("Accept-Ranges: " + ranges);

                //open the file so we can write the downloaded bytes into it
                try (FileChannel channel = FileChannel.open(
                        Path.of("downloaded.file"),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE
                )) {

                    InputStream input = response.body();

                    byte[] buffer = new byte[8192];
                    long position = start;
                    int bytesRead;

                    while ((bytesRead = input.read(buffer)) != -1) {

                        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead);

                        while (byteBuffer.hasRemaining()) {
                            position += channel.write(byteBuffer, position);
                        }
                    }

                    input.close();
                }

                System.out.println("written to downloaded.file");

            } else {
                System.err.println("Error: " + response.statusCode());
                System.err.println("Response body: " + response.body());
                System.err.println("Response headers: " + response.headers());

                return;
            }
        }
    }

    private long getFileSize(String link) throws Exception {

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
}
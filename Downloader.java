import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Downloader {

    private final HttpClient client;

    public Downloader() {
        //client which goes to server
        client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public void download(String link, long start, long end) throws Exception {

        //request is needed by the client to give to the server
        HttpRequest request = HttpRequest.newBuilder(URI.create(link))
                //giving instructions to the server
                .header("Range", "bytes=" + start + "-" + end)
                .build();

        //response is what the server gives back to the client
        HttpResponse<byte[]> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofByteArray()
        );

        if(response.statusCode() == 206) {
            System.out.println("Partial download successful");

            System.out.println("Bytes received: " + response.body().length);

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

                //put the downloaded bytes into a ByteBuffer
                ByteBuffer buffer = ByteBuffer.wrap(response.body());


                //write the bytes in the file starting from the position given
                channel.write(buffer, start);
            }

            System.out.println("written to downloaded.file");

        } else {
            System.err.println("Error: " + response.statusCode());
            System.err.println("Response body: " + response.body());
            System.err.println("Response headers: " + response.headers());
        }
    }
}


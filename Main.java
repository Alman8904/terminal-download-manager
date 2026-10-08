import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws Exception {
        //download link
        String link = "https://comicvine.gamespot.com/a/uploads/original/11143/111432035/7944488-1654581-invincible.77.20.jpg";

        //client which goes to server
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        //request is needed by the client to give to the server
        HttpRequest request = HttpRequest.newBuilder(URI.create(link)).build();

        //response is what the server gives back to the client
        //Path because java stores files in Path
        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(Path.of("downloaded.file")));

        if(response.statusCode() == 200) {
            System.out.println("Done: " + response.body());


            //get the size of the file
            long size = response.headers()
                    .firstValueAsLong("Content-Length")
                    .orElse(-1);


            //check what kind of ranges the server supports
            String ranges = response.headers()
                    .firstValue("Accept-Ranges")
                    .orElse("none");

            System.out.println("Content-Length: " + size);
            System.out.println("Accept-Ranges: " + ranges);
        } else {
            System.err.println("Error: " + response.statusCode());
            java.nio.file.Files.deleteIfExists(response.body());
        }
    }
}
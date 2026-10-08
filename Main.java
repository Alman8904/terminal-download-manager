import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Main {
    public static void main(String[] args) throws Exception {
        //download link
        String link = "https://comicvine.gamespot.com/a/uploads/original/11143/111432035/7944488-1654581-invincible.77.20.jpg";

        //create a downloader object
        Downloader downloader = new Downloader();

        //download the file
        downloader.download(link, 100000, 199999);
    }
}
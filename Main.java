import java.net.URI;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        String link;
        if (args.length > 0) {
            link = args[0];
        } else {
            System.out.print("Paste a link: ");
            link = new Scanner(System.in).nextLine().trim();
        }

        if (link.isEmpty()) {
            System.out.println("No link given.");
            return;
        }

        String name = args.length > 1 ? args[1] : fileNameFrom(link);
        new Downloader().download(link, Path.of(name));
    }

    private static String fileNameFrom(String link) {
        String path = URI.create(link).getPath();
        String name = path == null ? "" : path.substring(path.lastIndexOf('/') + 1);
        if (name.isEmpty() || name.equals(".") || name.equals("..")) {
            return "downloaded.file";
        }
        return name;
    }
}
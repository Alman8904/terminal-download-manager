import java.nio.file.Path;

public class Downloader {

    private final FileInspector fileInspector;
    private final ParallelDownloader parallelDownloader;
    private final SequentialDownloader sequentialDownloader;

    public Downloader() {
        fileInspector = new FileInspector();
        parallelDownloader = new ParallelDownloader();
        sequentialDownloader = new SequentialDownloader();
    }

    public void download(String link) throws Exception {
        Path destination = Path.of("downloaded.file");

        long fileSize = fileInspector.getFileSize(link);
        boolean supportsRanges = fileInspector.supportsRanges(link);

        System.out.println("File size: " + fileSize);
        System.out.println("Supports ranges: " + supportsRanges);

        if (supportsRanges && fileSize > 0) {
            System.out.println("Using parallel downloader");
            parallelDownloader.download(link, fileSize, destination);
        } else {
            System.out.println("Using sequential downloader");
            sequentialDownloader.download(link, destination);
        }
    }
}

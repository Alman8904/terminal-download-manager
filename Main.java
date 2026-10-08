public class Main {
    public static void main(String[] args) throws Exception {
        //download link
        String link = "https://comicvine.gamespot.com/a/uploads/original/11143/111432035/7944488-1654581-invincible.77.20.jpg";

        //create a downloader object
        Downloader downloader = new Downloader();

        //download the file
        downloader.download(link);
    }
}
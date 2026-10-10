public class Terminal {

    private static final String ESC = "\033[";

    public static void moveUp(int lines) {
        System.out.print(ESC + lines + "A");
    }

    public static void printLine(String text) {
        System.out.print(text + ESC + "K\n");
    }
}
import java.util.Map;

public class MagicCheck {

    private static final Map<String, byte[]> SIGNATURES = Map.of(
            "jpg",  new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            "jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            "png",  new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47},
            "gif",  new byte[]{'G', 'I', 'F', '8'},
            "pdf",  new byte[]{'%', 'P', 'D', 'F'},
            "zip",  new byte[]{0x50, 0x4B, 0x03, 0x04}
    );

    // returns null if fine, otherwise a reason to abort
    public static String check(String fileName, byte[] data, int len) {
        int dot = fileName.lastIndexOf('.');
        String ext = dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase();

        byte[] expected = SIGNATURES.get(ext);
        if (expected == null || startsWith(data, len, expected)) {
            return null;
        }

        boolean exe = startsWith(data, len, new byte[]{'M', 'Z'})
                || startsWith(data, len, new byte[]{0x7F, 'E', 'L', 'F'});

        return "file is named ." + ext + " but its content is "
                + (exe ? "an executable program" : "not a ." + ext + " file");
    }

    private static boolean startsWith(byte[] data, int len, byte[] sig) {
        if (len < sig.length) return false;
        for (int i = 0; i < sig.length; i++) {
            if (data[i] != sig[i]) return false;
        }
        return true;
    }
}
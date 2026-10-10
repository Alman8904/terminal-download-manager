import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class DownloadState {

    private final Path stateFile;
    private final Properties properties = new Properties();

    public DownloadState(Path stateFile) {
        this.stateFile = stateFile;
    }

    public void save() throws IOException {
        Path temporaryFile = stateFile.resolveSibling(
                stateFile.getFileName() + ".tmp"
        );

        try (var output = Files.newOutputStream(temporaryFile)) {
            properties.store(output, null);
        }

        try {
            Files.move(
                    temporaryFile,
                    stateFile,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE
            );
        } catch (java.nio.file.AtomicMoveNotSupportedException e) {
            Files.move(
                    temporaryFile,
                    stateFile,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    public void load() throws IOException {
        if (Files.exists(stateFile)) {
            try (var input = Files.newInputStream(stateFile)) {
                properties.load(input);
            }
        }
    }

    public void set(String key, String value) {
        properties.setProperty(key, value);
    }

    public String get(String key) {
        return properties.getProperty(key);
    }
}

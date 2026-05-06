package valorless.havenbags.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import valorless.havenbags.Main;
import valorless.valorlessutils.logging.Log;

public class ErrorLog {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss dd MMM yy");
    private static final String LOG_FILE_NAME = "data_error.log";

    public static void addLog(String log) {
        Path dataFolder = Main.plugin.getDataFolder().toPath();
        Path logFile = dataFolder.resolve(LOG_FILE_NAME);
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String line = String.format("[%s] %s%n", timestamp, log);

        try {
            Files.createDirectories(dataFolder);
            Files.writeString(logFile, line, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            Log.info(Main.plugin, "Logged 1 error.");
        } catch (IOException e) {
            Log.error(Main.plugin, "Failed to write to error.log: " + e.getMessage());
        }
    }
}

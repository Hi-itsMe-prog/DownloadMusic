import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;

public class AudioDownloader {

    private static final String DOWNLOAD_DIRECTORY = "downloads/";
    private static final String URL_LIST_FILE = "audio_links.txt";


    private static final String PLAYER_PATH = "cmd"; // будет использоваться через cmd /c start

    public static void main(String[] args) {
        try {
            Files.createDirectories(Paths.get(DOWNLOAD_DIRECTORY));

            try (BufferedReader urlReader = new BufferedReader(new FileReader(URL_LIST_FILE))) {
                String currentUrl;
                int fileIndex = 0;

                while ((currentUrl = urlReader.readLine()) != null) {
                    if (currentUrl.trim().isEmpty()) {
                        continue;
                    }

                    String outputPath = DOWNLOAD_DIRECTORY + "audio_" + fileIndex + ".mp3";
                    File outputFile = new File(outputPath);

                    try {
                        fetchAudioFile(currentUrl, outputPath);
                        System.out.println("Успешно загружено: " + outputPath);

                        ProcessBuilder pb = new ProcessBuilder(
                                "cmd", "/c", "start", "", outputPath
                        );
                        pb.start();
                        System.out.println("Файл запущен через системный плеер.");

                        fileIndex++;
                    } catch (IOException e) {
                        System.err.println("Ошибка при загрузке или воспроизведении: " + currentUrl + " — " + e.getMessage());
                    }
                }
            }


        } catch (IOException e) {
            System.err.println("Критическая ошибка при запуске: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void fetchAudioFile(String sourceUrl, String destinationPath) throws IOException {
        try (ReadableByteChannel sourceChannel = Channels.newChannel(new URL(sourceUrl).openStream());
             FileOutputStream output = new FileOutputStream(destinationPath)) {
            output.getChannel().transferFrom(sourceChannel, 0, Long.MAX_VALUE);
        }
    }
}
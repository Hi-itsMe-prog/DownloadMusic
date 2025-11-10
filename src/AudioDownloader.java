import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;

public class AudioDownloader {

    private static final String AUDIO_FOLDER = "audio/";
    private static final String URLS_FILE = "urls.txt";
    private static final int TIMEOUT_MS = 30000;

    public static void main(String[] args) {
        System.out.println("Запуск загрузчика аудиофайлов...");

        try {
            createDownloadDirectory();
            processAudioUrls();

            System.out.println("Все операции завершены!");

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createDownloadDirectory() throws IOException {
        Path directory = Paths.get(AUDIO_FOLDER);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
            System.out.println("Создана папка: " + AUDIO_FOLDER);
        }
    }

    private static void processAudioUrls() {
        int successCount = 0;
        int totalCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(URLS_FILE))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                totalCount++;
                String fileName = "track" + totalCount + ".mp3";

                if (downloadAudioFile(line, fileName)) {
                    successCount++;
                }
            }

        } catch (FileNotFoundException e) {
            System.err.println("Файл " + URLS_FILE + " не найден!");
            return;
        } catch (Exception e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }

        System.out.println("Успешно загружено: " + successCount + " из " + totalCount);
    }

    private static boolean downloadAudioFile(String audioUrl, String fileName) {
        String filePath = AUDIO_FOLDER + fileName;

        System.out.println("Загружаем: " + audioUrl);

        try {
            URL url = new URL(audioUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.err.println("Ошибка сервера: " + responseCode);
                return false;
            }

            try (ReadableByteChannel channel = Channels.newChannel(connection.getInputStream());
                 FileOutputStream output = new FileOutputStream(filePath)) {

                output.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
            }

            File downloadedFile = new File(filePath);
            if (downloadedFile.length() == 0) {
                System.err.println("Файл пуст: " + fileName);
                downloadedFile.delete();
                return false;
            }

            System.out.println("Сохранено: " + fileName);
            return true;

        } catch (IOException e) {
            System.err.println("Ошибка загрузки: " + audioUrl + " - " + e.getMessage());
            new File(filePath).delete();
            return false;
        }
    }
}
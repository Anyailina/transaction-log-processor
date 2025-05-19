package org.annill.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.annill.model.CustomDate;
import org.annill.model.Transaction;
import org.annill.util.FileUtils;

public class FileService {

    private static final Logger logger = Logger.getLogger(FileService.class.getName());

    public List<Path> findLogFiles(String searchPath, String extension) throws IOException {
        List<Path> logFiles = FileUtils.searchFile(searchPath, extension);
        if (logFiles.isEmpty()) {
            throw new IOException("Файлов не найдено");
        }
        logger.info("Найдено " + logFiles.size() + " лог-файлов");
        return logFiles;
    }

    public Path prepareOutputDirectory(String baseOutputPath, String subDir) throws IOException {
        Path basePath = Paths.get(baseOutputPath);
        if (!Files.exists(basePath)) {
            throw new IOException("Указанный путь не существует: " + baseOutputPath);
        }

        Path outputDir = basePath.resolve(subDir);
        FileUtils.deleteAllLogFiles(outputDir);
        Files.createDirectories(outputDir);
        logger.info("Подготовлена выходная директория: " + outputDir);
        return outputDir;
    }

    public void writeUserTransactionFiles(Path outputDir, Map<CustomDate, Transaction> transactions)
        throws IOException {
        int count = 0;
        for (Transaction t : transactions.values()) {
            Path userFilePath = outputDir.resolve(t.user() + ".log");
            FileUtils.writeToFile(userFilePath, t.toString());
            count++;
        }
        logger.info("Записано " + count + " файлов транзакций");
    }


    public void appendBalancesToFiles(Path outputDir, Map<String, Double> balances, String timestamp)
        throws IOException {
        for (Map.Entry<String, Double> entry : balances.entrySet()) {
            Path userFilePath = outputDir.resolve(entry.getKey() + ".log");
            String logLine = String.format("[%s] %s final balance %.2f%n",
                timestamp, entry.getKey(), entry.getValue());

            if (Files.exists(userFilePath)) {
                Files.write(userFilePath, logLine.getBytes(), StandardOpenOption.APPEND);
            }
        }
        logger.info("Дописаны балансы для " + balances.size() + " пользователей");
    }
}
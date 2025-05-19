package org.annill.service;


import java.nio.file.Files;
import org.annill.model.CustomDate;
import org.annill.model.Transaction;
import org.annill.util.DateFormatUtils;
import org.annill.util.TransactionLogParser;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

public class TransactionService {

    private static final String OUTPUT_SUBDIR = "transactions_by_users";
    private static final Logger logger = Logger.getLogger(TransactionService.class.getName());

    private final FileService fileService;
    private final BalanceService balanceService;

    public TransactionService() {
        this.fileService = new FileService();
        this.balanceService = new BalanceService();
    }

    public void processTransactions(String[] args) {
        try {
            validateArguments(args);

            List<Path> logFiles = fileService.findLogFiles(args[0], ".log");

            Path outputDir = fileService.prepareOutputDirectory(args[1], OUTPUT_SUBDIR);

            Map<CustomDate, Transaction> transactions = parseTransactions(logFiles);

            processTransactionsInternal(transactions);

            fileService.writeUserTransactionFiles(outputDir, transactions);
            writeFinalBalances(outputDir);

            logger.info("Обработка завершена успешно");

        } catch (IOException e) {
            logger.severe("Ошибка обработки: " + e.getMessage());
        }
    }

    private void validateArguments(String[] args) throws IllegalArgumentException {
        if (args == null || args.length != 2) {
            throw new IllegalArgumentException(
                "Необходимо указать два аргумента:\n" +
                    "1. Путь где искать файлы\n" +
                    "2. Путь куда сохранять результаты");
        }
    }

    private Map<CustomDate, Transaction> parseTransactions(List<Path> logFiles) throws IOException {
        Map<CustomDate, Transaction> transactions = new TreeMap<>();

        for (Path file : logFiles) {
            Files.lines(file)
                .map(TransactionLogParser::parseLogLine)
                .forEach(t -> transactions.put(new CustomDate(t.timestamp()), t));
        }

        logger.info("Распаршено " + transactions.size() + " транзакций");
        return transactions;
    }


    private void processTransactionsInternal(Map<CustomDate, Transaction> transactions) {
        transactions.values().forEach(balanceService::processTransaction);
        logger.info("Обработано " + transactions.size() + " транзакций");
    }


    private void writeFinalBalances(Path outputDir) throws IOException {
        String timestamp = DateFormatUtils.getFormattedTimestamp(LocalDateTime.now());
        fileService.appendBalancesToFiles(outputDir, balanceService.getBalances(), timestamp);
    }
}

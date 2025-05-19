package org.annill;

import org.annill.service.TransactionService;

public class Main {

    public static void main(String[] args) {
        TransactionService transactionService = new TransactionService();
        transactionService.processTransactions(args);
    }
}
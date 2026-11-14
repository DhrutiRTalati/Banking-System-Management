package com.banking.utilities;

import com.banking.model.Account;
import com.banking.model.Transaction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * StatementExporter handles file writing and statement export.
 * Demonstrates Unit 4: File Handling and Java I/O using BufferedWriter and PrintWriter.
 */
public final class StatementExporter {

    private StatementExporter() {}

    /**
     * Exports a formatted bank account statement to a text file.
     *
     * @param account      the account holder's account
     * @param transactions list of transactions to include
     * @param targetFile   destination file
     * @return true if export succeeded
     * @throws IOException on I/O error
     */
    public static boolean exportToTextFile(Account account, List<Transaction> transactions, File targetFile)
            throws IOException {
        if (targetFile.getParentFile() != null && !targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(targetFile)))) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            writer.println("================================================================================");
            writer.println("                       APEX DIGITAL BANKING SYSTEM                              ");
            writer.println("                          OFFICIAL ACCOUNT STATEMENT                            ");
            writer.println("================================================================================");
            writer.println("Generated At    : " + timestamp);
            writer.println("Account Holder  : " + account.getFullName());
            writer.println("Account Number  : " + account.getAccountNumber());
            writer.println("Account Type    : " + account.getAccountType().getDisplayName());
            writer.println("Account Status  : " + account.getStatus().getLabel());
            writer.println("Current Balance : " + CurrencyFormatter.format(account.getBalance()));
            writer.println("--------------------------------------------------------------------------------");
            writer.printf("%-19s | %-16s | %-14s | %-12s | %-12s | %s%n",
                    "Date & Time", "Reference", "Type", "Amount", "Balance After", "Description");
            writer.println("--------------------------------------------------------------------------------");

            if (transactions == null || transactions.isEmpty()) {
                writer.println("No recorded transactions found for this statement period.");
            } else {
                for (Transaction tx : transactions) {
                    String typeSign = tx.getType().isCredit() ? "+ " : "- ";
                    writer.printf("%-19s | %-16s | %-14s | %-12s | %-12s | %s%n",
                            tx.getFormattedTimestamp(),
                            tx.getTransactionRef() != null ? tx.getTransactionRef() : ("TXN-" + tx.getId()),
                            tx.getType().getDescription(),
                            typeSign + CurrencyFormatter.format(tx.getAmount()),
                            CurrencyFormatter.format(tx.getBalanceAfter()),
                            tx.getDescription() != null ? tx.getDescription() : "-"
                    );
                }
            }
            writer.println("--------------------------------------------------------------------------------");
            writer.println("                  End of Statement - Apex Digital Banking                       ");
            writer.println("================================================================================");
            writer.flush();
            return true;
        }
    }

    /**
     * Exports transactions in CSV format for spreadsheet analysis.
     */
    public static boolean exportToCsvFile(Account account, List<Transaction> transactions, File targetFile)
            throws IOException {
        if (targetFile.getParentFile() != null && !targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(targetFile)))) {
            writer.println("Date,Reference,Type,Amount,BalanceAfter,RefAccount,Description");
            if (transactions != null) {
                for (Transaction tx : transactions) {
                    writer.printf("\"%s\",\"%s\",\"%s\",%.2f,%.2f,\"%s\",\"%s\"%n",
                            tx.getFormattedTimestamp(),
                            tx.getTransactionRef() != null ? tx.getTransactionRef() : ("TXN-" + tx.getId()),
                            tx.getType().name(),
                            tx.getAmount(),
                            tx.getBalanceAfter(),
                            tx.getRefAccount() != null ? String.valueOf(tx.getRefAccount()) : "",
                            tx.getDescription() != null ? tx.getDescription().replace("\"", "\"\"") : ""
                    );
                }
            }
            writer.flush();
            return true;
        }
    }
}

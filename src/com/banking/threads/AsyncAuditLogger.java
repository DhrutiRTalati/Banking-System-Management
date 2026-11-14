package com.banking.threads;

import com.banking.dao.AuditLogDAO;
import com.banking.dao.impl.AuditLogDAOImpl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Demonstrates Multithreading, Synchronization, Producer-Consumer pattern,
 * and File I/O (Unit 4 & 5).
 *
 * Runs a background daemon thread that drains an asynchronous queue of audit events
 * and writes them to both a physical log file (logs/banking_audit.log) and the database.
 */
public final class AsyncAuditLogger implements Runnable {

    public static class AuditEntry {
        private final String eventType;
        private final String actor;
        private final String details;
        private final long timestamp;

        public AuditEntry(String eventType, String actor, String details) {
            this.eventType = eventType;
            this.actor = actor;
            this.details = details;
            this.timestamp = System.currentTimeMillis();
        }

        public String getFormatted() {
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));
            return String.format("[%s] [%s] Actor: %s | %s", time, eventType, actor, details);
        }
    }

    private static volatile AsyncAuditLogger instance;
    private final BlockingQueue<AuditEntry> queue = new LinkedBlockingQueue<>();
    private final AuditLogDAO auditLogDAO = new AuditLogDAOImpl();
    private final File logFile = new File("logs/banking_audit.log");
    private volatile boolean running = true;
    private Thread workerThread;

    private AsyncAuditLogger() {
        if (logFile.getParentFile() != null && !logFile.getParentFile().exists()) {
            logFile.getParentFile().mkdirs();
        }
        workerThread = new Thread(this, "AuditLoggerThread");
        workerThread.setDaemon(true);
        workerThread.start();
    }

    public static AsyncAuditLogger getInstance() {
        if (instance == null) {
            synchronized (AsyncAuditLogger.class) {
                if (instance == null) {
                    instance = new AsyncAuditLogger();
                }
            }
        }
        return instance;
    }

    /**
     * Non-blocking method to submit an audit entry.
     */
    public void log(String eventType, String actor, String details) {
        queue.offer(new AuditEntry(eventType, actor, details));
    }

    @Override
    public void run() {
        while (running || !queue.isEmpty()) {
            try {
                AuditEntry entry = queue.take(); // Blocks until an item is available
                // 1. Write to log file via Java I/O
                writeToFile(entry);
                // 2. Persist to DB audit table
                try {
                    auditLogDAO.log(entry.eventType, entry.actor, entry.details);
                } catch (Exception e) {
                    System.err.println("Audit log DB error: " + e.getMessage());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private synchronized void writeToFile(AuditEntry entry) {
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)))) {
            pw.println(entry.getFormatted());
            pw.flush();
        } catch (IOException e) {
            System.err.println("Failed to write to audit log file: " + e.getMessage());
        }
    }

    public void shutdown() {
        running = false;
        if (workerThread != null) {
            workerThread.interrupt();
        }
    }
}

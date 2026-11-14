package com.banking.threads;

import javax.swing.SwingWorker;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Generic SwingWorker implementation to execute banking tasks asynchronously,
 * preventing GUI freezing and ensuring a responsive user interface.
 *
 * Demonstrates Multithreading in Java Swing GUI (Unit 5).
 *
 * @param <T> the result type returned by this SwingWorker's doInBackground method
 */
public class TransactionWorker<T> extends SwingWorker<T, Void> {

    private final Supplier<T> task;
    private final Consumer<T> onSuccess;
    private final Consumer<Exception> onError;
    private final Runnable onComplete;

    public TransactionWorker(Supplier<T> task, Consumer<T> onSuccess, Consumer<Exception> onError) {
        this(task, onSuccess, onError, null);
    }

    public TransactionWorker(Supplier<T> task, Consumer<T> onSuccess,
                             Consumer<Exception> onError, Runnable onComplete) {
        this.task = task;
        this.onSuccess = onSuccess;
        this.onError = onError;
        this.onComplete = onComplete;
    }

    @Override
    protected T doInBackground() throws Exception {
        return task.get();
    }

    @Override
    protected void done() {
        try {
            T result = get();
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            if (onError != null) {
                onError.accept(cause instanceof Exception ? (Exception) cause : new Exception(cause));
            }
        } finally {
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }
}

package com.banking.model;

/**
 * SystemStats transfer object used by the Administrator Console to display
 * macro-level health metrics and liquidity reports.
 */
public class SystemStats {
    private final int totalCustomers;
    private final int totalAccounts;
    private final int activeAccounts;
    private final int frozenAccounts;
    private final double totalLiquidity;
    private final int totalTransactions;

    public SystemStats(int totalCustomers, int totalAccounts, int activeAccounts,
                       int frozenAccounts, double totalLiquidity, int totalTransactions) {
        this.totalCustomers = totalCustomers;
        this.totalAccounts = totalAccounts;
        this.activeAccounts = activeAccounts;
        this.frozenAccounts = frozenAccounts;
        this.totalLiquidity = totalLiquidity;
        this.totalTransactions = totalTransactions;
    }

    public int getTotalCustomers() {
        return totalCustomers;
    }

    public int getTotalAccounts() {
        return totalAccounts;
    }

    public int getActiveAccounts() {
        return activeAccounts;
    }

    public int getFrozenAccounts() {
        return frozenAccounts;
    }

    public double getTotalLiquidity() {
        return totalLiquidity;
    }

    public int getTotalTransactions() {
        return totalTransactions;
    }
}

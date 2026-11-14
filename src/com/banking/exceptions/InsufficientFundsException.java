package com.banking.exceptions;

/**
 * Thrown when an account has inadequate balance or would breach the minimum balance / overdraft limit.
 */
public class InsufficientFundsException extends BankingException {
    private final double availableBalance;
    private final double requestedAmount;

    public InsufficientFundsException(double availableBalance, double requestedAmount) {
        super("INSUFFICIENT_FUNDS",
                String.format("Insufficient funds: Available %.2f, Requested %.2f", availableBalance, requestedAmount));
        this.availableBalance = availableBalance;
        this.requestedAmount = requestedAmount;
    }

    public InsufficientFundsException(String customMessage) {
        super("INSUFFICIENT_FUNDS", customMessage);
        this.availableBalance = 0;
        this.requestedAmount = 0;
    }

    public double getAvailableBalance() {
        return availableBalance;
    }

    public double getRequestedAmount() {
        return requestedAmount;
    }
}

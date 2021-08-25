package com.db.awmd.challenge.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends Exception {

    public InsufficientFundsException(String accountId, BigDecimal amount) {
        super("Account: " + accountId +
                " does not have sufficient funds to perform a transfer of " + amount);
    }
}

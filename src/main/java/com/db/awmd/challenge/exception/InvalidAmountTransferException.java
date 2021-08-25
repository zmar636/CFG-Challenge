package com.db.awmd.challenge.exception;

public class InvalidAmountTransferException extends Exception {

    public InvalidAmountTransferException() {
        super("Transfer amount must be bigger than 0");
    }
}

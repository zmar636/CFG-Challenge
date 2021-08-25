package com.db.awmd.challenge.exception;

public class SameAccountTransferException extends Exception {

    public SameAccountTransferException() {
        super("Origin account is the same as destination account");
    }
}

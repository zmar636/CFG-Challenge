package com.db.awmd.challenge.exception;

public class NonexistentAccountException extends Exception {

    public NonexistentAccountException(String accountId) {
        super("Account: " + accountId + " does not exist");
    }

}

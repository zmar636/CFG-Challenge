package com.db.awmd.challenge.web;

import com.db.awmd.challenge.exception.InsufficientFundsException;
import com.db.awmd.challenge.exception.InvalidAmountTransferException;
import com.db.awmd.challenge.exception.NonexistentAccountException;
import com.db.awmd.challenge.exception.SameAccountTransferException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class AccountsControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {InsufficientFundsException.class, NonexistentAccountException.class,
            InvalidAmountTransferException.class, SameAccountTransferException.class})
    public ResponseEntity handleAccountsException(Exception e, WebRequest request) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}

package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.InsufficientFundsException;
import com.db.awmd.challenge.exception.InvalidAmountTransferException;
import com.db.awmd.challenge.exception.NonexistentAccountException;
import com.db.awmd.challenge.exception.SameAccountTransferException;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class AccountsService {

    @Getter
    private final AccountsRepository accountsRepository;
    private final NotificationService notificationService;

    @Autowired
    public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
        this.accountsRepository = accountsRepository;
        this.notificationService = notificationService;
    }

    public void createAccount(Account account) {
        this.accountsRepository.createAccount(account);
    }

    public Account getAccount(String accountId) {
        return this.accountsRepository.getAccount(accountId);
    }

    public void transfer(String accountFromId, String accountToId, BigDecimal amount) throws InsufficientFundsException,
            NonexistentAccountException, InvalidAmountTransferException, SameAccountTransferException {

        Account fromAccount = getAccount(accountFromId);
        if (fromAccount == null) {
            throw new NonexistentAccountException(accountFromId);
        }

        if (accountFromId.equals(accountToId)) {
            throw new SameAccountTransferException();
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountTransferException();
        }

        synchronized (fromAccount) {
            fromAccount.withdraw(amount);
        }

        Account accountTo = getAccount(accountToId);

        synchronized (accountTo) {
            accountTo.deposit(amount);
        }

        notificationService.notifyAboutTransfer(fromAccount, "Debit of " + amount + " to account: " + accountToId);
        notificationService.notifyAboutTransfer(accountTo, "Deposit of " + amount + " from account: " + accountFromId);
    }
}

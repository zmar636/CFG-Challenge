package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.InsufficientFundsException;
import com.db.awmd.challenge.exception.InvalidAmountTransferException;
import com.db.awmd.challenge.exception.SameAccountTransferException;
import com.db.awmd.challenge.exception.NonexistentAccountException;
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
    if(fromAccount == null) {
      throw new NonexistentAccountException("Account: " + accountFromId + " does not exist");
    }

    if(accountFromId.equals(accountToId)) {
      throw new SameAccountTransferException("Origin account is the same as destination account");
    }

    if(amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new InvalidAmountTransferException("Transfer amount must be bigger than 0");
    }

    if(fromAccount.getBalance().compareTo(amount) < 0) {
      throw new InsufficientFundsException("Account: " + accountFromId +
              " does not have sufficient funds to perform a transfer of " + amount);
    }

    BigDecimal fromAccountBeforeDebt = fromAccount.getBalance();
    synchronized (fromAccount) {
      fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
    }

    Account accountTo = getAccount(accountToId);
    BigDecimal toAccountBeforeDeposit = accountTo.getBalance();

    synchronized (accountTo) {
      accountTo.setBalance(accountTo.getBalance().add(amount));
    }

    notificationService.notifyAboutTransfer(fromAccount, "Debit of " + amount + " to account: " + accountToId);
    notificationService.notifyAboutTransfer(accountTo, "Deposit of " + amount + " from account: " + accountFromId);

    log.info(toAccountBeforeDeposit + "-"  + getAccount(accountToId).getBalance());
  }
}

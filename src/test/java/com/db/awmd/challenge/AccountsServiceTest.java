package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.*;
import com.db.awmd.challenge.service.AccountsService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.db.awmd.challenge.service.NotificationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;

  @MockBean
  private NotificationService notificationService;

  @Test
  public void addAccount() {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  public void addAccount_failsOnDuplicateId() {
    String uniqueId = UUID.randomUUID().toString();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }

  }


  @Test
  public void transfer_failsOnNonexistentAccount() throws SameAccountTransferException, InsufficientFundsException, InvalidAmountTransferException {
    String uniqueId = "Id-" + System.currentTimeMillis();
    try {
      accountsService.transfer(uniqueId, uniqueId, BigDecimal.ONE);
      fail("Should have failed when transferring from an nonexistent account");
    } catch (NonexistentAccountException e) {
      assertThat(e.getMessage()).isEqualTo("Account: " + uniqueId + " does not exist");
    }
  }

  @Test
  public void transfer_failsOnTransferringToSameAccount() throws InsufficientFundsException, InvalidAmountTransferException, NonexistentAccountException {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    try {
      accountsService.transfer(account.getAccountId(), account.getAccountId(), BigDecimal.ONE);
      fail("Should have failed when transferring to the same account");
    } catch (SameAccountTransferException e) {
      assertThat(e.getMessage()).isEqualTo("Origin account is the same as destination account");
    }
  }

  @Test
  public void transfer_failsOnInsufficientFundsAccount() throws InvalidAmountTransferException, NonexistentAccountException, SameAccountTransferException {
    String uniqueId = UUID.randomUUID().toString();
    Account account = new Account(uniqueId);
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    String anotherUniqueId = UUID.randomUUID().toString();
    Account anotherAccount = new Account(anotherUniqueId);
    anotherAccount.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(anotherAccount);

    try {
      accountsService.transfer(account.getAccountId(), anotherAccount.getAccountId(), BigDecimal.valueOf(2000));
      fail("Should have failed when account has insufficient amount");
    } catch (InsufficientFundsException e) {
      assertThat(e.getMessage()).isEqualTo("Account: " + account.getAccountId() +
              " does not have sufficient funds to perform a transfer of " + BigDecimal.valueOf(2000));
    }
  }

  @Test
  public void transfer_failsOnNegativeTransferAmount() throws NonexistentAccountException, SameAccountTransferException, InsufficientFundsException {
    String uniqueId = UUID.randomUUID().toString();
    Account account = new Account(uniqueId);
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    String anotherUniqueId = UUID.randomUUID().toString();
    Account anotherAccount = new Account(anotherUniqueId);
    anotherAccount.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(anotherAccount);

    try {
      accountsService.transfer(account.getAccountId(), anotherAccount.getAccountId(), BigDecimal.valueOf(-1));
      fail("Should have failed when amount is less than 1");
    } catch (InvalidAmountTransferException e) {
      assertThat(e.getMessage()).isEqualTo("Transfer amount must be bigger than 0");
    }
  }

  @Test
  public void transfer_failsOnZeroTransferAmount() throws NonexistentAccountException, SameAccountTransferException, InsufficientFundsException {
    String uniqueId = UUID.randomUUID().toString();
    Account account = new Account(uniqueId);
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    String anotherUniqueId = UUID.randomUUID().toString();
    Account anotherAccount = new Account(anotherUniqueId);
    anotherAccount.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(anotherAccount);

    try {
      accountsService.transfer(account.getAccountId(), anotherAccount.getAccountId(), BigDecimal.ZERO);
      fail("Should have failed when amount is less than 1");
    } catch (InvalidAmountTransferException e) {
      assertThat(e.getMessage()).isEqualTo("Transfer amount must be bigger than 0");
    }
  }

  @Test
  public void transfer_notificationServiceIsCalled() throws InvalidAmountTransferException, SameAccountTransferException, InsufficientFundsException, NonexistentAccountException {
    doNothing().when(notificationService).notifyAboutTransfer(isA(Account.class), isA(String.class));

    String uniqueId = UUID.randomUUID().toString();
    Account account = new Account(uniqueId);
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    String anotherUniqueId = UUID.randomUUID().toString();
    Account anotherAccount = new Account(anotherUniqueId);
    anotherAccount.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(anotherAccount);

    accountsService.transfer(uniqueId, anotherUniqueId, BigDecimal.valueOf(500));

    verify(notificationService, times(1))
            .notifyAboutTransfer(account, "Debit of " + BigDecimal.valueOf(500) + " to account: " + anotherUniqueId);

    verify(notificationService, times(1))
            .notifyAboutTransfer(anotherAccount, "Deposit of " + BigDecimal.valueOf(500) + " from account: " + uniqueId);
  }

  @Test
  public void transfer_balancesAreCorrectAfterTransfer() throws InvalidAmountTransferException, SameAccountTransferException, InsufficientFundsException, NonexistentAccountException {
    doNothing().when(notificationService).notifyAboutTransfer(isA(Account.class), isA(String.class));

    String uniqueId = UUID.randomUUID().toString();
    Account account = new Account(uniqueId);
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    String anotherUniqueId = UUID.randomUUID().toString();
    Account anotherAccount = new Account(anotherUniqueId);
    anotherAccount.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(anotherAccount);

    accountsService.transfer(uniqueId, anotherUniqueId, BigDecimal.valueOf(500));

    assertThat(account.getBalance()).isEqualTo(BigDecimal.valueOf(500));
    assertThat(anotherAccount.getBalance()).isEqualTo(BigDecimal.valueOf(1500));
  }

  /**
   * 5 threads will transfer an amount from account A to B
   * 5 threads will transfer an amount from account B to A
   * This will be a concurrent execution, when their job is done the balance must be the same before the threads execution
   * @throws InterruptedException
   */
  @Test
  public void transfer_noDeadLock() throws InterruptedException {
    String uniqueId = UUID.randomUUID().toString();
    Account account = new Account(uniqueId);
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    String anotherUniqueId = UUID.randomUUID().toString();
    Account anotherAccount = new Account(anotherUniqueId);
    anotherAccount.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(anotherAccount);

    Runnable runnable = () -> {
      try {
        accountsService.transfer(uniqueId, anotherUniqueId, BigDecimal.valueOf(10));
      } catch (Exception e) {
        e.printStackTrace();
      }
    };

    Runnable anotherRunnable = () -> {
      try {
        accountsService.transfer(anotherUniqueId, uniqueId, BigDecimal.valueOf(10));
      } catch (Exception e) {
        e.printStackTrace();
      }
    };

    List<Thread> threads = new ArrayList<>(10);

    for(int i=0; i<10; i++) {
      if( (i%2) == 0) {
        threads.add(new Thread(runnable));
      } else {
        threads.add(new Thread(anotherRunnable));
      }
    }

    for(Thread t : threads) {
      t.start();
    }

    for(Thread t : threads) {
      t.join();
    }

    assertThat(account.getBalance()).isEqualTo(BigDecimal.valueOf(1000));
    assertThat(anotherAccount.getBalance()).isEqualTo(BigDecimal.valueOf(1000));

  }
}

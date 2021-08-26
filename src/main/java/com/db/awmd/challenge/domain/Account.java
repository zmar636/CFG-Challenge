package com.db.awmd.challenge.domain;

import com.db.awmd.challenge.exception.InsufficientFundsException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class Account {

  @NotNull
  @NotEmpty
  private final String accountId;

  @NotNull
  @Min(value = 0, message = "Initial balance must be positive.")
  private BigDecimal balance;

  public Account(String accountId) {
    this.accountId = accountId;
    this.balance = BigDecimal.ZERO;
  }

  public void withdraw(BigDecimal amount) throws InsufficientFundsException {
    if (balance.compareTo(amount) < 0) {
      throw new InsufficientFundsException(accountId, amount);
    }

    balance = balance.subtract(amount);
  }

  public void deposit(BigDecimal amount) {
    balance = balance.add(amount);
  }

  @JsonCreator
  public Account(@JsonProperty("accountId") String accountId,
    @JsonProperty("balance") BigDecimal balance) {
    this.accountId = accountId;
    this.balance = balance;
  }
}

package com.db.awmd.challenge.domain;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transfer {

    String accountFromId;
    String accountToId;
    BigDecimal amount;
}

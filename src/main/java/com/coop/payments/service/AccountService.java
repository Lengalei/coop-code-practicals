package com.coop.payments.service;

import com.coop.payments.dto.AccountResponse;
import com.coop.payments.dto.BalanceResponse;
import com.coop.payments.dto.CreateAccountRequest;
import com.coop.payments.entity.Account;
import com.coop.payments.exception.AccountNotFoundException;
import com.coop.payments.repository.AccountRepository;
import com.coop.payments.util.MaskUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public AccountResponse create(CreateAccountRequest req) {
        BigDecimal opening = req.openingBalance() == null
                ? BigDecimal.ZERO
                : req.openingBalance().setScale(2, RoundingMode.HALF_UP);

        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .ownerName(req.ownerName().trim())
                .balance(opening)
                .build();
        accountRepository.save(account);

        log.info("Account created account={} opening={}", MaskUtil.mask(account.getAccountNumber()), opening);
        return AccountResponse.from(account);
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        log.info("Balance enquiry account={}", MaskUtil.mask(accountNumber));
        return new BalanceResponse(
                account.getAccountNumber(),
                account.getOwnerName(),
                account.getCurrency(),
                account.getBalance(),
                String.format(Locale.US, "%s %,.2f", account.getCurrency(), account.getBalance()),
                Instant.now());
    }

    private String generateAccountNumber() {
        String number;
        do {
            number = String.format("%010d", ThreadLocalRandom.current().nextLong(1_000_000_000L, 10_000_000_000L));
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }
}

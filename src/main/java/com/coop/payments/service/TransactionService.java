package com.coop.payments.service;

import com.coop.payments.dto.TransactionRequest;
import com.coop.payments.dto.TransactionResponse;
import com.coop.payments.entity.Account;
import com.coop.payments.entity.CustomerTransaction;
import com.coop.payments.entity.TransactionType;
import com.coop.payments.exception.AccountNotFoundException;
import com.coop.payments.exception.DuplicateResourceException;
import com.coop.payments.exception.InsufficientFundsException;
import com.coop.payments.repository.AccountRepository;
import com.coop.payments.repository.CustomerTransactionRepository;
import com.coop.payments.util.MaskUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final CustomerTransactionRepository transactionRepository;

    @Transactional
    public TransactionResponse record(TransactionRequest req) {
        log.info("Recording transaction ref={} type={} channel={} account={}",
                req.reference(), req.type(), req.channel(), MaskUtil.mask(req.accountNumber()));

        if (transactionRepository.existsByReference(req.reference())) {
            throw new DuplicateResourceException("Transaction reference already processed: " + req.reference());
        }

        Account account = accountRepository.findByAccountNumberForUpdate(req.accountNumber())
                .orElseThrow(() -> new AccountNotFoundException(req.accountNumber()));

        BigDecimal amount = req.amount().setScale(2, RoundingMode.HALF_UP);

        if (req.type() == TransactionType.WITHDRAWAL) {
            if (account.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException("Insufficient funds for withdrawal on account "
                        + MaskUtil.mask(req.accountNumber()));
            }
            account.setBalance(account.getBalance().subtract(amount));
        } else {
            account.setBalance(account.getBalance().add(amount));
        }

        CustomerTransaction saved = transactionRepository.save(CustomerTransaction.builder()
                .reference(req.reference())
                .accountNumber(req.accountNumber())
                .type(req.type())
                .channel(req.channel())
                .amount(amount)
                .balanceAfter(account.getBalance())
                .msisdn(req.msisdn())
                .description(req.description())
                .build());

        log.info("Transaction recorded ref={} newBalance={}", saved.getReference(), saved.getBalanceAfter());
        return TransactionResponse.from(saved);
    }
}

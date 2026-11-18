package com.coop.payments.service;

import com.coop.payments.dto.TransferRequest;
import com.coop.payments.dto.TransferResponse;
import com.coop.payments.entity.Account;
import com.coop.payments.entity.Transfer;
import com.coop.payments.entity.TransferStatus;
import com.coop.payments.exception.AccountNotFoundException;
import com.coop.payments.exception.DuplicateResourceException;
import com.coop.payments.exception.InsufficientFundsException;
import com.coop.payments.exception.InvalidTransferException;
import com.coop.payments.messaging.TransferCompletedEvent;
import com.coop.payments.repository.AccountRepository;
import com.coop.payments.repository.TransferRepository;
import com.coop.payments.util.MaskUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public TransferResponse transfer(TransferRequest req) {
        log.info("Transfer requested ref={} from={} to={} amount={}", req.reference(),
                MaskUtil.mask(req.fromAccount()), MaskUtil.mask(req.toAccount()), req.amount());

        if (req.fromAccount().equals(req.toAccount())) {
            throw new InvalidTransferException("Source and destination accounts must be different");
        }
        if (transferRepository.existsByReference(req.reference())) {
            throw new DuplicateResourceException("Transfer reference already used: " + req.reference());
        }

        String firstNumber = req.fromAccount().compareTo(req.toAccount()) < 0
                ? req.fromAccount() : req.toAccount();
        String secondNumber = req.fromAccount().equals(firstNumber)
                ? req.toAccount() : req.fromAccount();

        Account first = accountRepository.findByAccountNumberForUpdate(firstNumber)
                .orElseThrow(() -> new AccountNotFoundException(firstNumber));
        Account second = accountRepository.findByAccountNumberForUpdate(secondNumber)
                .orElseThrow(() -> new AccountNotFoundException(secondNumber));

        Account source = req.fromAccount().equals(firstNumber) ? first : second;
        Account target = req.toAccount().equals(firstNumber) ? first : second;

        if (!source.getCurrency().equals(target.getCurrency())) {
            throw new InvalidTransferException("Cross-currency transfers are not supported");
        }

        BigDecimal amount = req.amount().setScale(2, RoundingMode.HALF_UP);
        if (source.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds on account " + MaskUtil.mask(req.fromAccount()));
        }

        source.setBalance(source.getBalance().subtract(amount));
        target.setBalance(target.getBalance().add(amount));

        Transfer saved = transferRepository.save(Transfer.builder()
                .reference(req.reference())
                .sourceAccount(req.fromAccount())
                .destinationAccount(req.toAccount())
                .amount(amount)
                .description(req.description())
                .status(TransferStatus.COMPLETED)
                .build());

        eventPublisher.publishEvent(new TransferCompletedEvent(saved.getReference(), saved.getSourceAccount(),
                saved.getDestinationAccount(), saved.getAmount(), source.getCurrency(), saved.getCreatedAt()));

        log.info("Transfer completed ref={}", saved.getReference());
        return TransferResponse.from(saved);
    }
}

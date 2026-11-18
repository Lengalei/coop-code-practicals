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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    private static final String FROM = "1000000001";
    private static final String TO = "1000000002";

    @Mock AccountRepository accountRepository;
    @Mock TransferRepository transferRepository;
    @Mock ApplicationEventPublisher eventPublisher;
    @InjectMocks TransferService service;

    private Account source;
    private Account target;

    @BeforeEach
    void setUp() {
        source = Account.builder().accountNumber(FROM).ownerName("Alice").balance(new BigDecimal("1000.00")).build();
        target = Account.builder().accountNumber(TO).ownerName("Brian").balance(new BigDecimal("200.00")).build();
    }

    private TransferRequest request(String from, String to, String amount) {
        return new TransferRequest("REF-1", from, to, new BigDecimal(amount), "test");
    }

    @Test
    void transfer_movesMoneyAndPublishesEvent() {
        when(transferRepository.existsByReference("REF-1")).thenReturn(false);
        when(accountRepository.findByAccountNumberForUpdate(FROM)).thenReturn(Optional.of(source));
        when(accountRepository.findByAccountNumberForUpdate(TO)).thenReturn(Optional.of(target));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(inv -> inv.getArgument(0));

        TransferResponse response = service.transfer(request(FROM, TO, "300.00"));

        assertThat(source.getBalance()).isEqualByComparingTo("700.00");
        assertThat(target.getBalance()).isEqualByComparingTo("500.00");
        assertThat(response.status()).isEqualTo(TransferStatus.COMPLETED);
        verify(eventPublisher).publishEvent(any(TransferCompletedEvent.class));
    }

    @Test
    void transfer_rejectsInsufficientFunds_andChangesNothing() {
        when(transferRepository.existsByReference("REF-1")).thenReturn(false);
        when(accountRepository.findByAccountNumberForUpdate(FROM)).thenReturn(Optional.of(source));
        when(accountRepository.findByAccountNumberForUpdate(TO)).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> service.transfer(request(FROM, TO, "5000.00")))
                .isInstanceOf(InsufficientFundsException.class);

        assertThat(source.getBalance()).isEqualByComparingTo("1000.00");
        assertThat(target.getBalance()).isEqualByComparingTo("200.00");
        verify(transferRepository, never()).save(any());
    }

    @Test
    void transfer_rejectsSameAccount() {
        assertThatThrownBy(() -> service.transfer(request(FROM, FROM, "10.00")))
                .isInstanceOf(InvalidTransferException.class);
    }

    @Test
    void transfer_rejectsDuplicateReference() {
        when(transferRepository.existsByReference("REF-1")).thenReturn(true);

        assertThatThrownBy(() -> service.transfer(request(FROM, TO, "10.00")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void transfer_rejectsUnknownAccount() {
        when(transferRepository.existsByReference("REF-1")).thenReturn(false);
        when(accountRepository.findByAccountNumberForUpdate(FROM)).thenReturn(Optional.of(source));
        when(accountRepository.findByAccountNumberForUpdate(TO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.transfer(request(FROM, TO, "10.00")))
                .isInstanceOf(AccountNotFoundException.class);
    }
}

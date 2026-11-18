package com.coop.payments.controller;

import com.coop.payments.dto.AccountResponse;
import com.coop.payments.dto.BalanceResponse;
import com.coop.payments.dto.CreateAccountRequest;
import com.coop.payments.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Validated
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@Valid @RequestBody CreateAccountRequest request) {
        return accountService.create(request);
    }

    @GetMapping("/{accountNumber}/balance")
    public BalanceResponse balance(
            @PathVariable @Pattern(regexp = "\\d{10}", message = "must be a 10-digit account number") String accountNumber) {
        return accountService.getBalance(accountNumber);
    }
}

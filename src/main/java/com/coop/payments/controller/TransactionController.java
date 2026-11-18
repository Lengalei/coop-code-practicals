package com.coop.payments.controller;

import com.coop.payments.dto.TransactionRequest;
import com.coop.payments.dto.TransactionResponse;
import com.coop.payments.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse record(@Valid @RequestBody TransactionRequest request) {
        return transactionService.record(request);
    }
}

package com.coop.payments.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;


@Entity
@Table(name = "customer_transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, unique = true, length = 40)
    private String reference;

    @Column(nullable = false, length = 10)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Channel channel;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(length = 15)
    private String msisdn;

    @Column(length = 200)
    private String description;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}

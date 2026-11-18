package com.coop.payments.repository;

import com.coop.payments.entity.CustomerTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerTransactionRepository extends JpaRepository<CustomerTransaction, Long> {
    boolean existsByReference(String reference);
}

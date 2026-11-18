package com.coop.payments.repository;

import com.coop.payments.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    boolean existsByReference(String reference);
}

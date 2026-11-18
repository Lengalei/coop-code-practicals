package com.coop.payments.config;

import com.coop.payments.entity.Account;
import com.coop.payments.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Slf4j
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedAccounts(AccountRepository repo) {
        return args -> {
            if (repo.count() > 0) return;
            repo.save(Account.builder().accountNumber("1000000001").ownerName("Alice Wanjiru")
                    .balance(new BigDecimal("50000.00")).build());
            repo.save(Account.builder().accountNumber("1000000002").ownerName("Brian Otieno")
                    .balance(new BigDecimal("10000.00")).build());
            log.info("Seeded demo accounts 1000000001 and 1000000002");
        };
    }
}

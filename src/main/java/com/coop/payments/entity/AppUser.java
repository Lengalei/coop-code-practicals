package com.coop.payments.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;


    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 20)
    private String role;
}

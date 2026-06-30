package com.nearkart.userservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    private String password;

    @Column(unique = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role = Role.CUSTOMER;

    private boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Role {
        CUSTOMER, MERCHANT, DELIVERY_AGENT, ADMIN
    }
}

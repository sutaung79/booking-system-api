package com.codetest.bookingsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String token;

  @OneToOne(targetEntity = AppUser.class, fetch = FetchType.EAGER)
  @JoinColumn(nullable = false, name = "user_id")
  private AppUser user;

  @Column(nullable = false)
  private Instant expiryDate;

  public boolean isExpired() {
    return Instant.now().isAfter(expiryDate);
  }
}
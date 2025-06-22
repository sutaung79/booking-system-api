package com.codetest.bookingsystem.repository;

import com.codetest.bookingsystem.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
	
	Optional<PasswordResetToken> findByToken(String token);

	Optional<PasswordResetToken> findByUserId(Long userId);
}
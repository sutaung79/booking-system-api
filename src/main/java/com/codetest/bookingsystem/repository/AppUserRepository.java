package com.codetest.bookingsystem.repository;

import com.codetest.bookingsystem.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
	
	Optional<AppUser> findByUsername(String username);

	Optional<AppUser> findByEmail(String email);

	Boolean existsByUsername(String username);

	Boolean existsByEmail(String email);
}
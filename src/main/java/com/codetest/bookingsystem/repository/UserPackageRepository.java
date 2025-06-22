package com.codetest.bookingsystem.repository;

import com.codetest.bookingsystem.enums.Country;
import com.codetest.bookingsystem.model.UserPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserPackageRepository extends JpaRepository<UserPackage, Long> {

	List<UserPackage> findByUserId(Long userId);

	@Query("SELECT up FROM UserPackage up JOIN up.creditPackage cp "
			+ "WHERE up.user.id = :userId AND cp.country = :country AND up.remainingCredits >= :requiredCredits AND up.expiryDate >= :today "
			+ "ORDER BY up.expiryDate ASC")
	List<UserPackage> findActivePackagesForBooking(@Param("userId") Long userId, @Param("country") Country country,
			@Param("requiredCredits") int requiredCredits, @Param("today") LocalDate today);
}
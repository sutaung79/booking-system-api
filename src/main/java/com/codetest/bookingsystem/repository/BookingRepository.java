package com.codetest.bookingsystem.repository;

import com.codetest.bookingsystem.enums.BookingStatus;
import com.codetest.bookingsystem.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

	int countByClassScheduleIdAndStatus(Long classScheduleId, BookingStatus status);

	Optional<Booking> findByUserIdAndClassScheduleId(Long userId, Long classScheduleId);

	@Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.status = 'BOOKED' AND b.classSchedule.startTime < :newEndTime AND b.classSchedule.endTime > :newStartTime")
	List<Booking> findOverlappingBookings(@Param("userId") Long userId,
			@Param("newStartTime") LocalDateTime newStartTime, @Param("newEndTime") LocalDateTime newEndTime);

	List<Booking> findByUserId(Long userId);
}
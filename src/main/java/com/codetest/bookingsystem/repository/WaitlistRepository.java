package com.codetest.bookingsystem.repository;

import com.codetest.bookingsystem.enums.WaitlistStatus;
import com.codetest.bookingsystem.model.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {

	Optional<Waitlist> findFirstByClassScheduleIdAndStatusOrderByCreatedAtAsc(Long classScheduleId,
			WaitlistStatus status);

	List<Waitlist> findByClassScheduleIdAndStatus(Long classScheduleId, WaitlistStatus status);

	Optional<Waitlist> findByUserIdAndClassScheduleId(Long userId, Long classScheduleId);
}
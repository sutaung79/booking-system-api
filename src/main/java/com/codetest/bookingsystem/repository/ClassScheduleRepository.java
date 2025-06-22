package com.codetest.bookingsystem.repository;

import com.codetest.bookingsystem.enums.Country;
import com.codetest.bookingsystem.model.ClassSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Long> {
	
	List<ClassSchedule> findByCountryAndStartTimeAfter(Country country, LocalDateTime dateTime);

	// Find classes that ended within a given time range
	List<ClassSchedule> findByEndTimeBetween(LocalDateTime start, LocalDateTime end);
}
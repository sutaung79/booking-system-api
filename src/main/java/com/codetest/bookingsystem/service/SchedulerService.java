package com.codetest.bookingsystem.service;

import com.codetest.bookingsystem.enums.WaitlistStatus;
import com.codetest.bookingsystem.model.ClassSchedule;
import com.codetest.bookingsystem.model.UserPackage;
import com.codetest.bookingsystem.model.Waitlist;
import com.codetest.bookingsystem.repository.UserPackageRepository;
import com.codetest.bookingsystem.repository.WaitlistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchedulerService {

	private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

	@Autowired
	private WaitlistRepository waitlistRepository;

	@Autowired
	private UserPackageRepository userPackageRepository;

	@Transactional
	public void refundCreditForWaitlist(Waitlist waitlistEntry) {
		UserPackage userPackage = waitlistEntry.getUserPackage();
		ClassSchedule classSchedule = waitlistEntry.getClassSchedule();

		userPackage.setRemainingCredits(userPackage.getRemainingCredits() + classSchedule.getRequiredCredits());
		userPackageRepository.save(userPackage);

		waitlistEntry.setStatus(WaitlistStatus.CREDIT_REFUNDED);
		waitlistRepository.save(waitlistEntry);

		logger.info("Refunded {} credits to user {} for waitlisted class '{}'.", classSchedule.getRequiredCredits(),
				waitlistEntry.getUser().getId(), classSchedule.getClassName());
	}
}
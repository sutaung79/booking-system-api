package com.codetest.bookingsystem.scheduler;

import com.codetest.bookingsystem.enums.WaitlistStatus;
import com.codetest.bookingsystem.model.ClassSchedule;
import com.codetest.bookingsystem.model.Waitlist;
import com.codetest.bookingsystem.repository.ClassScheduleRepository;
import com.codetest.bookingsystem.repository.WaitlistRepository;
import com.codetest.bookingsystem.service.SchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ClassCompletionScheduler {

  private static final Logger logger = LoggerFactory.getLogger(ClassCompletionScheduler.class);

  @Autowired
  private ClassScheduleRepository classScheduleRepository;

  @Autowired
  private WaitlistRepository waitlistRepository;

  @Autowired
  private SchedulerService schedulerService;

  /**
   * This job runs every 15 minutes to process classes that have recently ended.
   * It finds users who were still on the waitlist for these classes and refunds
   * their credits.
   */
  @Scheduled(cron = "0 0/15 * * * ?") // Runs every 15 minutes
  public void processEndedClassesAndWaitlists() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime fifteenMinutesAgo = now.minusMinutes(15);
    logger.info("Running scheduled job to process ended classes between {} and {}", fifteenMinutesAgo, now);

    List<ClassSchedule> endedClasses = classScheduleRepository.findByEndTimeBetween(fifteenMinutesAgo, now);

    for (ClassSchedule classSchedule : endedClasses) {
      List<Waitlist> waitingUsers = waitlistRepository.findByClassScheduleIdAndStatus(classSchedule.getId(),
          WaitlistStatus.WAITING);
      waitingUsers.forEach(schedulerService::refundCreditForWaitlist);
    }
  }
}
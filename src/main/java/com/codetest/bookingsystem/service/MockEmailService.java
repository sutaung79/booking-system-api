package com.codetest.bookingsystem.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MockEmailService {

	private static final Logger logger = LoggerFactory.getLogger(MockEmailService.class);

	@Value("${app.mock.email.enabled:true}")
	private boolean mockEmailEnabled;

	@Value("${app.mock.email.send-success:true}")
	private boolean mockEmailSendSuccess;

	public boolean sendVerifyEmail(String toEmail, String subject, String body) {
		if (!mockEmailEnabled) {
			logger.info("Real email sending logic would be here for: {}", toEmail);
			// In a real application, integrate with an actual email service (e.g., SendGrid, JavaMailSender)
		}
		logger.info("Mock Email Service: Sending verification email to {} with subject '{}'. Body: '{}'. Success: {}",
				toEmail, subject, body, mockEmailSendSuccess);
		return mockEmailSendSuccess;
	}
}
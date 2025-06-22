package com.codetest.bookingsystem.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class MockPaymentService {

	private static final Logger logger = LoggerFactory.getLogger(MockPaymentService.class);

	@Value("${app.mock.payment.enabled:true}")
	private boolean mockPaymentEnabled;

	@Value("${app.mock.payment.add-card-success:true}")
	private boolean mockAddCardSuccess;

	@Value("${app.mock.payment.charge-success:true}")
	private boolean mockChargeSuccess;

	public boolean addPaymentCard(String cardNumber, String cardHolderName, String expiryDate, String cvv) {
		logger.info("Mock Payment Service: Adding payment card. Success: {}", mockAddCardSuccess);
		return mockAddCardSuccess;
	}

	public boolean paymentCharge(String transactionId, BigDecimal amount, String currency) {
		logger.info("Mock Payment Service: Charging amount {} {}. Success: {}", amount, currency, mockChargeSuccess);
		return mockChargeSuccess;
	}
}
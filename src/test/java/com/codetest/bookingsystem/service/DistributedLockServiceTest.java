package com.codetest.bookingsystem.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DistributedLockServiceTest {

	@Mock
	private StringRedisTemplate redisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@InjectMocks
	private DistributedLockService distributedLockService;

	private final Long CLASS_SCHEDULE_ID = 1L;
	private final String LOCK_KEY = "lock:class:" + CLASS_SCHEDULE_ID;

	@BeforeEach
	void setUp() {
		// This setup is moved into the tests that need it to avoid UnnecessaryStubbingException.
	}

	@Test
	void acquireLock_shouldReturnTrue_whenLockIsSuccessfullyAcquired() {
		// Arrange
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		// When setIfAbsent is called with our lock key, a value, and a duration, return true
		when(valueOperations.setIfAbsent(eq(LOCK_KEY), eq("locked"), any(Duration.class))).thenReturn(true);

		// Act
		boolean result = distributedLockService.acquireLock(CLASS_SCHEDULE_ID);

		// Assert
		assertTrue(result, "Should return true when lock is acquired");
		verify(valueOperations).setIfAbsent(eq(LOCK_KEY), eq("locked"), any(Duration.class));
	}

	@Test
	void acquireLock_shouldReturnFalse_whenLockIsAlreadyHeld() {
		// Arrange
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		// When setIfAbsent is called, return false, simulating that the lock is already held
		when(valueOperations.setIfAbsent(eq(LOCK_KEY), eq("locked"), any(Duration.class))).thenReturn(false);

		// Act
		boolean result = distributedLockService.acquireLock(CLASS_SCHEDULE_ID);

		// Assert
		assertFalse(result, "Should return false when lock is already held");
	}

	@Test
	void releaseLock_shouldCallDeleteOnRedisTemplate() {
		// Act
		distributedLockService.releaseLock(CLASS_SCHEDULE_ID);

		// Assert
		// Verify that the delete method was called on the redisTemplate with the correct lock key
		verify(redisTemplate).delete(LOCK_KEY);
	}
}
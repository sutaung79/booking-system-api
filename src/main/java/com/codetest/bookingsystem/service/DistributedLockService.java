package com.codetest.bookingsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class DistributedLockService {

	private static final String LOCK_PREFIX = "lock:class:";
	private static final long LOCK_TIMEOUT_SECONDS = 10; // Lock timeout to prevent deadlocks

	@Autowired
	private StringRedisTemplate redisTemplate;

	public boolean acquireLock(Long classScheduleId) {
		String lockKey = LOCK_PREFIX + classScheduleId;
		return Boolean.TRUE.equals(
				redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", Duration.ofSeconds(LOCK_TIMEOUT_SECONDS)));
	}

	public void releaseLock(Long classScheduleId) {
		String lockKey = LOCK_PREFIX + classScheduleId;
		redisTemplate.delete(lockKey);
	}
}
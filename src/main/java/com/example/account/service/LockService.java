package com.example.account.service;

import java.util.concurrent.TimeUnit;


import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import com.example.account.exception.AccountException;
import com.example.account.type.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LockService {
	private final RedissonClient redissonClient;
	
	public void Lock(String accountNumber) {
		RLock lock = redissonClient.getLock(getLockKey(accountNumber));//락 키 생성
		log.debug("Trying lock for accountNumber : {}", accountNumber);
		
		try {
			boolean isLock = lock.tryLock(1,15,TimeUnit.SECONDS);//lock걸리고 15초뒤 해체!
			if(!isLock) {
				log.error("=====Lock acquisition failed=====");
				throw new AccountException(ErrorCode.ACCOUNT_TRANSACTION_LOCK);
			}
		} catch (AccountException e) {
			throw e;
		} catch (Exception e) {
			log.error("Redis lock failed");
		}

	}
	
	public void unLock(String accountNumber) {
		log.debug("Unlock for accountNumber : {}", accountNumber);
		redissonClient.getLock(getLockKey(accountNumber)).unlock();
	}
	
	private String getLockKey(String accountNumber) {
		return "ACLK:"+accountNumber;
	}

}

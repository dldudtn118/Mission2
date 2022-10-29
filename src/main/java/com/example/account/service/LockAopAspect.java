package com.example.account.service;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.example.account.aop.AccountLockIdInterface;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LockAopAspect {
	private final LockService lockservice;
	
	@Around("@annotation(com.example.account.aop.AccountLock) && args(request)")
	public Object aroundMethod(ProceedingJoinPoint pjp,AccountLockIdInterface request) throws Throwable {
		//lock 취득시도
		lockservice.Lock(request.getAccountNumber());//request가 use cancel 2개라서 인터페이스로 가져옴
		try {
			return pjp.proceed();
		}finally {
			//무조건 lock 해제
			lockservice.unLock(request.getAccountNumber());
		}
	}

}

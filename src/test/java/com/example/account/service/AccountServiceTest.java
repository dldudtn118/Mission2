package com.example.account.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.account.domain.Account;
import com.example.account.type.AccountStatus;

@SpringBootTest
class AccountServiceTest {
	
//	@Autowired
//	private AccountService accountService;
//
//	@BeforeEach
//	void init() {
//		//accountService.createAccount(); //테스트 한번하면 객체를 또 새로만든다.
//	}
//	
//	@Test
//	@DisplayName("Test1번 이름변경!")
//	void testGetAccount() {
//		Account account = accountService.getAccount(1L);//db pk 자동생성1번
//		
//		assertEquals("40000", account.getAccountNumber());
//		assertEquals(AccountStatus.IN_USE, account.getAccountStatus());
//	}
//	
//	@Test
//	void testGetAccount2() {
//		Account account = accountService.getAccount(2L);
//		
//		assertEquals("40000", account.getAccountNumber());
//		assertEquals(AccountStatus.IN_USE, account.getAccountStatus());
//	}

}

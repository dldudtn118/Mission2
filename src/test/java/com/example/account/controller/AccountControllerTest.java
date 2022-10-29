package com.example.account.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.account.domain.Account;
import com.example.account.dto.AccountDto;
import com.example.account.dto.CreateAccount;
import com.example.account.dto.DeleteAccount;
import com.example.account.exception.AccountException;
import com.example.account.service.AccountService;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AccountController.class)
class AccountControllerTest {
	//@WebMvcTest는 @Controller같은 웹과 관련된 빈만 주입되며 
	//@Service와 같은 일반적인 @Component는 생성되지 않는 특성 때문에
	//해당 컨트롤러를 생성하는 데 필요한 다른 빈을 정의하지 못해 발생한다.
	//따라서 이런 경우에는 @MockBean을 사용해서 필요한 의존성을 채워주어야 한다
	//@MockBean으로 빈등록하면 자동으로 주입된다.
	
	//Spring Boot Container가 테스트 시에 필요하고, 
	//Bean이 Container에 존재한다면 @MockBean을 사용하고 
	//아닌 경우에는 @Mock을 사용한다.
	//서비스모키토테스에서는 interface를 가져와야하는데 인터페이스는 빈이 아니다 그래서
	//mock를 사용하고 주입을 해주었다.
	@MockBean
	private AccountService accountService;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	
	@Test
	void successCreateAccount() throws Exception{
		//given
		given(accountService.createAccount(anyLong(),anyLong()))
			.willReturn(AccountDto.builder()
					.userId(1L)
					.accountNumber("1234567890")
					.registeredAt(LocalDateTime.now())
					.unRegisteredAt(LocalDateTime.now())
					.build());
		
		//when
		//then
		mockMvc.perform(post("/account")
				.contentType(MediaType.APPLICATION_JSON)//헤더
				.content(objectMapper.writeValueAsString(//바디
						//objectMapper는 java 객체랑 json바꿀때 사용하는 라이브러리클래스 writeValueAsString는문자열로바꿔줌
						new CreateAccount.Request(333L,1111L)//이부분은 given에서 anylong줘서 아무값이나 상관없음 아래는 대신같아야함,
				)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value(1))
				.andExpect(jsonPath("$.accountNumber").value("1234567890"))
				.andDo(print());
		
		
	}
	
	@Test
	void successGetAccount() throws Exception{
		//given
		given(accountService.getAccount(anyLong()))
				.willReturn(Account.builder()
						.accountNumber("3456")
						.accountStatus(AccountStatus.IN_USE)
						.build());
			
		
		//when
		//then
		mockMvc.perform(get("/account/876"))
				.andDo(print())
				.andExpect(jsonPath("$.accountNumber").value("3456"))
				.andExpect(jsonPath("$.accountStatus").value("IN_USE"))
				.andExpect(status().isOk());
	}
	
	@Test
	void successDeleteAccount() throws Exception{
		//given
		given(accountService.deleteAccount(anyLong(),anyString()))
			.willReturn(AccountDto.builder()
					.userId(1L)
					.accountNumber("1234567890")
					.registeredAt(LocalDateTime.now())
					.unRegisteredAt(LocalDateTime.now())
					.build());
		
		//when
		//then
		mockMvc.perform(delete("/account")
				.contentType(MediaType.APPLICATION_JSON)//헤더
				.content(objectMapper.writeValueAsString(//바디
						//objectMapper는 java 객체랑 json바꿀때 사용하는 라이브러리클래스 writeValueAsString는문자열로바꿔줌
						new DeleteAccount.Request(3333L,"0987654321")//이부분은 given에서 anylong줘서 아무값이나 상관없음 아래는 대신같아야함,
				)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value(1))
				.andExpect(jsonPath("$.accountNumber").value("1234567890"))
				.andDo(print());
		
		
	}
	
	@Test
	void successGetAccountsByUserId() throws Exception{
		//given
		List<AccountDto> accountDtos = 
				Arrays.asList(
						AccountDto.builder()
							.accountNumber("1234567890")
							.balance(1000L).build(),
						AccountDto.builder()
							.accountNumber("1111111111")
							.balance(2000L).build(),
						AccountDto.builder()
							.accountNumber("2222222222")
							.balance(3000L).build()
				);
		given(accountService.getAccountByUserId(anyLong()))
			.willReturn(accountDtos);
						
		//when
		//then
		mockMvc.perform(get("/account?user_id=1"))
				.andDo(print())
				.andExpect(jsonPath("$[0].accountNumber").value("1234567890"))
				.andExpect(jsonPath("$[0].balance").value(1000))
				.andExpect(jsonPath("$[1].accountNumber").value("1111111111"))
				.andExpect(jsonPath("$[1].balance").value(2000))
				.andExpect(jsonPath("$[2].accountNumber").value("2222222222"))
				.andExpect(jsonPath("$[2].balance").value(3000));
		
	}
	
	@Test
	void failGetAccount() throws Exception{
		//given
		given(accountService.getAccount(anyLong()))
				.willThrow(new AccountException(ErrorCode.ACCOUNT_NOT_FOUNT));
			
		
		//when
		//then
		mockMvc.perform(get("/account/876"))
				.andDo(print())
				.andExpect(jsonPath("$.errorCode").value("ACCOUNT_NOT_FOUNT"))
				.andExpect(jsonPath("$.errorMessage").value("계좌가 없습니다."))
				.andExpect(status().isOk());
	}
	
	

}

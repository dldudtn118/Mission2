package com.example.account.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;

@ExtendWith(MockitoExtension.class)
class AccountServiceMockitoTest {
	
	@Mock
	private AccountRepository accountRepository; //가짜주입
	
	@Mock
	private AccountUserRepository accountUserRepository;
	
	@InjectMocks
	private AccountService accountService; //accountRepository를 서비스에 넣어줌
	
	@Test
	void createAccountSuccess(){
		//given
		AccountUser user = AccountUser.builder()
						.id(12L)
						.name("pobi").build();
		
		given(accountUserRepository.findById(anyLong()))
			.willReturn(Optional.of(user));
		given(accountRepository.findFirstByOrderByIdDesc())
			.willReturn(Optional.of(Account.builder()
						.accountNumber("1000000012").build()));
		given(accountRepository.save(any()))
			.willReturn(Account.builder()
						.accountUser(user)
						.accountNumber("1000000015").build());
		ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
		
		//when
		AccountDto accountDto = accountService.createAccount(1L, 1000L);
		//then
		verify(accountRepository, times(1)).save(captor.capture());
		assertEquals(12L, accountDto.getUserId());
		assertEquals("1000000013", captor.getValue().getAccountNumber());
	}
	
	@Test
	void createFirstAccount(){
		//given
		AccountUser user = AccountUser.builder()
						.id(15L)
						.name("pobi").build();
		
		given(accountUserRepository.findById(anyLong()))
			.willReturn(Optional.of(user));
		given(accountRepository.findFirstByOrderByIdDesc())
			.willReturn(Optional.empty());//초기값이 없을때 테스트
		given(accountRepository.save(any()))
			.willReturn(Account.builder()
						.accountUser(user)
						.accountNumber("1000000015").build());
		ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
		
		//when
		AccountDto accountDto = accountService.createAccount(1L, 1000L);
		//then
		verify(accountRepository, times(1)).save(captor.capture());
		assertEquals(15L, accountDto.getUserId());
		assertEquals("1000000000", captor.getValue().getAccountNumber());
		
	}
	
	@Test
	@DisplayName("해당 유저 없음 - 계좌 생성 실패")
	void createAccount_UserNotFound(){
		//given
		given(accountUserRepository.findById(anyLong()))
			.willReturn(Optional.empty());//유저없을떄
		
		//when
		AccountException accountException = assertThrows(AccountException.class,
				() -> accountService.createAccount(1L, 1000L));
				
		//then
		assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
	}
	
	@Test
	@DisplayName("유저 당 최대 계좌는 10개")
	void createAccount_maxAccountIs10(){
		//given
		AccountUser user = AccountUser.builder()
				.id(15L)
				.name("pobi").build();
		
		given(accountUserRepository.findById(anyLong()))
				.willReturn(Optional.of(user));
		given(accountRepository.countByAccountUser(any()))
				.willReturn(10);
		
		//when
		AccountException accountException = assertThrows(AccountException.class,
				() -> accountService.createAccount(1L, 1000L));
		
		//then
		assertEquals(ErrorCode.MAX_ACCOUNT_PER_USER_10, accountException.getErrorCode());
	}
	
	@Test
	void deleteAccountSuccess(){
		//given
		AccountUser user = AccountUser.builder()
						.id(12L)
						.name("pobi").build();
		
		given(accountUserRepository.findById(anyLong()))
			.willReturn(Optional.of(user));
		given(accountRepository.findByAccountNumber(anyString()))
			.willReturn(Optional.of(Account.builder()
						.accountUser(user)
						.balance(0L)
						.accountNumber("1000000012").build()));
		ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
		
		//when
		AccountDto accountDto = accountService.deleteAccount(1L, "1234567890");
		
		//then
		verify(accountRepository, times(1)).save(captor.capture());
		assertEquals(12L, accountDto.getUserId());
		assertEquals("1000000012", captor.getValue().getAccountNumber());
		assertEquals(AccountStatus.UN_REGISTERED, captor.getValue().getAccountStatus());	
	}
	
	@Test
	@DisplayName("해당 유저 없음 - 계좌 해지 실패")
	void deleteAccount_UserNotFound(){
		//given
		given(accountUserRepository.findById(anyLong()))
			.willReturn(Optional.empty());//유저없을떄
		
		//when
		AccountException accountException = assertThrows(AccountException.class,
				() -> accountService.deleteAccount(1L, "1234567890"));
				
		//then
		assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
	}
	
	@Test
	@DisplayName("해당 계좌 없음 - 계좌 해지 실패")
	void deleteAccount_AccountNotFound(){
		//given
		AccountUser user = AccountUser.builder()
						.id(12L)
						.name("pobi").build();
		
		given(accountUserRepository.findById(anyLong()))
			.willReturn(Optional.of(user));
		given(accountRepository.findByAccountNumber(anyString()))
			.willReturn(Optional.empty());
		
		//when
		AccountException accountException = assertThrows(AccountException.class,
				() -> accountService.deleteAccount(1L, "1234567890"));
				
		//then
		assertEquals(ErrorCode.ACCOUNT_NOT_FOUNT, accountException.getErrorCode());
	}
	
	@Test
	@DisplayName("계좌 소유주가 다름")
	void deleteAccountFailed_userUnMatch(){
		//given
		AccountUser pobi = AccountUser.builder()
				.id(12L)
				.name("pobi").build();
		AccountUser harry = AccountUser.builder()
				.id(13L)
				.name("Harry").build();
		
		given(accountUserRepository.findById(anyLong()))
			.willReturn(Optional.of(pobi));
		given(accountRepository.findByAccountNumber(anyString()))
			.willReturn(Optional.of(Account.builder()
						.accountUser(harry)
						.balance(0L)
						.accountNumber("1000000012").build()));
		
		//when
		AccountException accountException = assertThrows(AccountException.class,
				() -> accountService.deleteAccount(1L, "1234567890"));
				
		//then
		assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, accountException.getErrorCode());
	}
	
	@Test
	@DisplayName("해지 계좌는 잔액이 없어야 한다.")
	void deleteAccountFailed_balanceNotEmpty(){
		//given
		AccountUser pobi = AccountUser.builder()
				.id(12L)
				.name("pobi").build();
		
		given(accountUserRepository.findById(anyLong()))
			.willReturn(Optional.of(pobi));
		given(accountRepository.findByAccountNumber(anyString()))
			.willReturn(Optional.of(Account.builder()
						.accountUser(pobi)
						.balance(100L)
						.accountNumber("1000000012").build()));
		
		//when
		AccountException accountException = assertThrows(AccountException.class,
				() -> accountService.deleteAccount(1L, "1234567890"));
				
		//then
		assertEquals(ErrorCode.BALANCE_NOT_EMPTY, accountException.getErrorCode());
	}
	
	@Test
	@DisplayName("해지 계좌는 해지할 수 없다.")
	void deleteAccountFailed_alreadyUnregistered(){
		//given
		AccountUser pobi = AccountUser.builder()
				.id(12L)
				.name("pobi").build();
		
		given(accountUserRepository.findById(anyLong()))
			.willReturn(Optional.of(pobi));
		given(accountRepository.findByAccountNumber(anyString()))
			.willReturn(Optional.of(Account.builder()
						.accountUser(pobi)
						.accountStatus(AccountStatus.UN_REGISTERED)
						.balance(0L)
						.accountNumber("1000000012").build()));
		
		//when
		AccountException accountException = assertThrows(AccountException.class,
				() -> accountService.deleteAccount(1L, "1234567890"));
				
		//then
		assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, accountException.getErrorCode());
	}
	
	@Test
	void successGetAccountsByUserId(){
		//given
		AccountUser pobi = AccountUser.builder()
				.id(12L)
				.name("pobi").build();
		List<Account> accounts = 
				Arrays.asList(
						Account.builder()
							.accountUser(pobi)
							.accountNumber("1111111111")
							.balance(1000L).build(),
						Account.builder()
							.accountUser(pobi)
							.accountNumber("2222222222")
							.balance(2000L).build(),
						Account.builder()
							.accountUser(pobi)
							.accountNumber("3333333333")
							.balance(3000L).build()
				);
		given(accountUserRepository.findById(anyLong()))
			.willReturn(Optional.of(pobi));
		given(accountRepository.findByAccountUser(any()))
			.willReturn(accounts);
		
		//when
		List<AccountDto> accountDtos = accountService.getAccountByUserId(1L);
		
		//then
		assertEquals(3, accountDtos.size());
		assertEquals("1111111111", accountDtos.get(0).getAccountNumber());
		assertEquals(1000, accountDtos.get(0).getBalance());
		assertEquals("2222222222", accountDtos.get(1).getAccountNumber());
		assertEquals(2000, accountDtos.get(1).getBalance());
		assertEquals("3333333333", accountDtos.get(2).getAccountNumber());
		assertEquals(3000, accountDtos.get(2).getBalance());
	}
	
	@Test
	void failedToGetAccounts(){
		//given
		given(accountUserRepository.findById(anyLong()))
			.willReturn(Optional.empty());
	
		//when
		AccountException accountException = assertThrows(AccountException.class,
			() -> accountService.getAccountByUserId(1L));
			
		//then
			assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
	}
	
	
	
	
	
//	@Test
//	@DisplayName("계좌 조회 성공")
//	void testXXX(){
//		//given
//		given(accountRepository.findById(anyLong()))
//			.willReturn(Optional.of(Account.builder()
//							.accountStatus(AccountStatus.UN_REGISTERED)
//							.accountNumber("65789")
//							.build()));
//		
//		//long값을 담을 빈박스를 하나생성함. 
//		ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
//		//when
//		Account account = accountService.getAccount(4555L);
//		
//		//then
//		//서비스에서 생성한 accountRepository 인스턴스의 메소드가 몇번호출되었는가 테스트 given에서 파인드아이드1번 세이브0번
//		//anyLong은 롱이나 널이 호출될걸 예상하지만 정확한값은 신경안쓴다. 아무롱값이나 다가능하다는뜻.
//		verify(accountRepository, times(1)).findById(captor.capture());//fingByid인자에 뭐가들어가는지 캡쳐
//		verify(accountRepository, times(0)).save(any());
//		
//		assertEquals(4555L, captor.getValue());
//		assertNotEquals(1234L, captor.getValue());
//		
//		assertEquals("65789", account.getAccountNumber());
//		assertEquals(AccountStatus.UN_REGISTERED, account.getAccountStatus());
//	}
//	
//	@Test
//	@DisplayName("계좌 조회 실패(음수로 조회)")
//	void testFail(){
//		//given
//		//when
//		//Account account = accountService.getAccount(-10L); 이렇게 값주면 에러가
//		//예외처리된경우에는 예외처리로 넘어가서 테스트가 진행이안되고 끝난다. exception의경우
//		RuntimeException exception = assertThrows(RuntimeException.class,
//				()-> accountService.getAccount(-10L));
//		
//		//then
//		
//		assertEquals("Minus", exception.getMessage());
//	}


}

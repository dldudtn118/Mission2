package com.example.account.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Optional;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.domain.Transaction;
import com.example.account.dto.TransactionDto;
import com.example.account.dto.UseBalance;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.TransactionRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
	public static final long CANCEL_AMOUNT=200L;
	@Mock
	private TransactionRepository transactionRepository;
	
	@Mock
	private AccountRepository accountRepository;
	
	@Mock
	private AccountUserRepository accountUserRepository;
	
	@InjectMocks
	private TransactionService transactionService;

	@Test
	void successUseBalance(){
		//given
		AccountUser user = AccountUser.builder()
				.id(12L)
				.name("pobi")
				.build();
		
		Account account = Account.builder().
				accountUser(user)
				.accountStatus(AccountStatus.IN_USE)
				.balance(10000L)
				.accountNumber("1000000012")
				.build();
		
		given(accountUserRepository.findById(anyLong()))
			.willReturn(Optional.of(user));
		given(accountRepository.findByAccountNumber(anyString()))
			.willReturn(Optional.of(account));
		given(transactionRepository.save(any()))
			.willReturn(Transaction.builder()
					.account(account)
					.transactionType(TransactionType.USE)
					.transactionResultType(TransactionResultType.S)
					.transactionId("transactionId")
					.transactedAt(LocalDateTime.now())
					.amount(1000L)
					.balanceSnapshot(9000L)
					.build());
		ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
			
		
		// when
		TransactionDto transactionDto = transactionService.useBalance(1L, "1000000000", 200L);
		
		//then
		verify(transactionRepository, times(1)).save(captor.capture());
		assertEquals(200L, captor.getValue().getAmount());
		assertEquals(9800L, captor.getValue().getBalanceSnapshot());
		assertEquals(TransactionResultType.S, transactionDto.getTransactionResultType());
		assertEquals(TransactionType.USE, transactionDto.getTransactionType());
		assertEquals(9000L, transactionDto.getBalanceSnapshot());
		assertEquals(1000L, transactionDto.getAmount());
	}
	
	@Test
	@DisplayName("해당 유저 없음 - 잔액 사용 실패")
	void useBalance_UserNotFound(){
		//given
		given(accountUserRepository.findById(anyLong()))
			.willReturn(Optional.empty());//유저없을떄
		
		//when
		AccountException accountException = assertThrows(AccountException.class,
				() -> transactionService.useBalance(1L,"1000000000",1000L));
				
		//then
		assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
	}
	
	@Test
	@DisplayName("해당 계좌 없음 - 잔액 사용 실패")
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
				() -> transactionService.useBalance(1L,"1000000000",1000L));
				
		//then
		assertEquals(ErrorCode.ACCOUNT_NOT_FOUNT, accountException.getErrorCode());
	}
	
	@Test
	@DisplayName("계좌 소유주가 다름 - 잔액 사용 실패")
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
				() -> transactionService.useBalance(1L,"1000000000",1000L));
				
		//then
		assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, accountException.getErrorCode());
	}
	
	@Test
	@DisplayName("해지 계좌는 사용할 수 없다.")
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
				() -> transactionService.useBalance(1L,"1000000000",1000L));
				
		//then
		assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, accountException.getErrorCode());
	}
	
	@Test
	@DisplayName("거래 금액이 잔액보다 큰 경우")
	void exceedAmount_UseBalance(){
		//given
		AccountUser user = AccountUser.builder()
				.id(12L)
				.name("pobi")
				.build();
		
		Account account = Account.builder().
				accountUser(user)
				.accountStatus(AccountStatus.IN_USE)
				.balance(100L)
				.accountNumber("1000000012")
				.build();
		
		given(accountUserRepository.findById(anyLong()))
			.willReturn(Optional.of(user));
		given(accountRepository.findByAccountNumber(anyString()))
			.willReturn(Optional.of(account));
			
		
		// when
		AccountException accountException = assertThrows(AccountException.class,
				() -> transactionService.useBalance(1L,"1000000000",1000L));
		
		//then
		assertEquals(ErrorCode.AMOUNT_EXCEED_BALANCE, accountException.getErrorCode());
		verify(transactionRepository, times(0)).save(any());
	}
	
	@Test
	@DisplayName("실패 트랜잭션 저장 성공")
	void saveFailedUseTransaction(){
		//given
		AccountUser user = AccountUser.builder()
				.id(12L)
				.name("pobi")
				.build();
		
		Account account = Account.builder().
				accountUser(user)
				.accountStatus(AccountStatus.IN_USE)
				.balance(10000L)
				.accountNumber("1000000012")
				.build();
		
		given(accountRepository.findByAccountNumber(anyString()))
			.willReturn(Optional.of(account));
		given(transactionRepository.save(any()))
			.willReturn(Transaction.builder()
					.account(account)
					.transactionType(TransactionType.USE)
					.transactionResultType(TransactionResultType.S)
					.transactionId("transactionId")
					.transactedAt(LocalDateTime.now())
					.amount(1000L)
					.balanceSnapshot(9000L)
					.build());
		ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
			
		
		// when
		transactionService.saveFailedUseTransaction("1000000000", 200L);
		
		//then
		verify(transactionRepository, times(1)).save(captor.capture());
		assertEquals(200L, captor.getValue().getAmount());
		assertEquals(10000L, captor.getValue().getBalanceSnapshot());
		assertEquals(TransactionResultType.F, captor.getValue().getTransactionResultType());
	}
	
	@Test
	void successCancelBalance(){
		//given
		AccountUser user = AccountUser.builder()
				.id(12L)
				.name("pobi")
				.build();
		
		Account account = Account.builder().
				accountUser(user)
				.accountStatus(AccountStatus.IN_USE)
				.balance(10000L)
				.accountNumber("1000000000")
				.build();
		Transaction transaction = Transaction.builder()
				.account(account)
				.transactionType(TransactionType.USE)
				.transactionResultType(TransactionResultType.S)
				.transactionId("transactionId")
				.transactedAt(LocalDateTime.now())
				.amount(CANCEL_AMOUNT)
				.balanceSnapshot(10000L)
				.build();
		
		given(transactionRepository.findByTransactionId(anyString()))
			.willReturn(Optional.of(transaction));
		given(accountRepository.findByAccountNumber(anyString()))
			.willReturn(Optional.of(account));
		given(transactionRepository.save(any()))
			.willReturn(Transaction.builder()
					.account(account)
					.transactionType(TransactionType.CANCEL)
					.transactionResultType(TransactionResultType.S)
					.transactionId("transactionIdForCancel")
					.transactedAt(LocalDateTime.now())
					.amount(CANCEL_AMOUNT)
					.balanceSnapshot(10000L)
					.build());
		ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
			
		
		// when
		TransactionDto transactionDto = transactionService.cancelBalance(
				"transactionId", "1000000000", CANCEL_AMOUNT);
		
		//then
		verify(transactionRepository, times(1)).save(captor.capture());
		assertEquals(CANCEL_AMOUNT, captor.getValue().getAmount());
		assertEquals(10000L+CANCEL_AMOUNT, captor.getValue().getBalanceSnapshot());
		assertEquals(TransactionResultType.S, transactionDto.getTransactionResultType());
		assertEquals(TransactionType.CANCEL, transactionDto.getTransactionType());
		assertEquals(10000L, transactionDto.getBalanceSnapshot());
		assertEquals(CANCEL_AMOUNT, transactionDto.getAmount());
	}
	
	@Test
	@DisplayName("해당 계좌 없음 - 잔액 사용 취소 실패")
	void cancelTransaction_AccountNotFound(){
		//given
		given(transactionRepository.findByTransactionId(anyString()))
		.willReturn(Optional.of(Transaction.builder().build()));
		given(accountRepository.findByAccountNumber(anyString()))
			.willReturn(Optional.empty());
		
		//when
		AccountException accountException = assertThrows(AccountException.class,
				() -> transactionService.cancelBalance("transactionId","1000000000",1000L));
				
		//then
		assertEquals(ErrorCode.ACCOUNT_NOT_FOUNT, accountException.getErrorCode());
	}
	
	@Test
	@DisplayName("원 사용 거래 없음 - 잔액 사용 취소 실패")
	void cancelTransaction_TransactionNotFound(){
		//given
		given(transactionRepository.findByTransactionId(anyString()))
			.willReturn(Optional.empty());
		
		//when
		AccountException accountException = assertThrows(AccountException.class,
				() -> transactionService.cancelBalance("transactionId","1000000000",1000L));
				
		//then
		assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, accountException.getErrorCode());
	}
	
	@Test
	@DisplayName("거래와 계좌가 매칭실패 - 잔액 사용 취소 실패")
	void cancelTransaction_TransactionAccountUnMatch(){
		//given
		AccountUser user = AccountUser.builder()
				.id(12L)
				.name("pobi")
				.build();
		
		Account account = Account.builder().
				id(1L)
				.accountUser(user)
				.accountStatus(AccountStatus.IN_USE)
				.balance(10000L)
				.accountNumber("1000000000")
				.build();
		Account accountNotUse = Account.builder().
				accountUser(user)
				.id(2L)
				.accountStatus(AccountStatus.IN_USE)
				.balance(10000L)
				.accountNumber("1000000000")
				.build();
		Transaction transaction = Transaction.builder()
				.account(account)
				.transactionType(TransactionType.USE)
				.transactionResultType(TransactionResultType.S)
				.transactionId("transactionId")
				.transactedAt(LocalDateTime.now())
				.amount(CANCEL_AMOUNT)
				.balanceSnapshot(10000L)
				.build();
		given(transactionRepository.findByTransactionId(anyString()))
			.willReturn(Optional.of(transaction));
		given(accountRepository.findByAccountNumber(anyString()))
			.willReturn(Optional.of(accountNotUse));
		
		//when
		AccountException accountException = assertThrows(AccountException.class,
				() -> transactionService.cancelBalance("transactionId","1000000000",CANCEL_AMOUNT));
				
		//then
		assertEquals(ErrorCode.TRANSACTION_ACCOUNT_UN_MATCH, accountException.getErrorCode());
	}
	
	@Test
	@DisplayName("거래금액과 최소금액이 다름 - 잔액 사용 취소 실패")
	void cancelTransaction_CancelMustFully(){
		//given
		AccountUser user = AccountUser.builder()
				.id(12L)
				.name("pobi")
				.build();
		
		Account account = Account.builder().
				id(1L)
				.accountUser(user)
				.accountStatus(AccountStatus.IN_USE)
				.balance(10000L)
				.accountNumber("1000000000")
				.build();

		Transaction transaction = Transaction.builder()
				.account(account)
				.transactionType(TransactionType.USE)
				.transactionResultType(TransactionResultType.S)
				.transactionId("transactionId")
				.transactedAt(LocalDateTime.now())
				.amount(CANCEL_AMOUNT+1000L)
				.balanceSnapshot(10000L)
				.build();
		given(transactionRepository.findByTransactionId(anyString()))
			.willReturn(Optional.of(transaction));
		given(accountRepository.findByAccountNumber(anyString()))
			.willReturn(Optional.of(account));
		
		//when
		AccountException accountException = assertThrows(AccountException.class,
				() -> transactionService.cancelBalance("transactionId","1000000000",CANCEL_AMOUNT));
				
		//then
		assertEquals(ErrorCode.CANCEL_MUST_FULLY, accountException.getErrorCode());
	}
	
	@Test
	@DisplayName("취소는 1년까지만 가능 - 잔액 사용 취소 실패")
	void cancelTransaction_tooOldOrder(){
		//given
		AccountUser user = AccountUser.builder()
				.id(12L)
				.name("pobi")
				.build();
		
		Account account = Account.builder().
				id(1L)
				.accountUser(user)
				.accountStatus(AccountStatus.IN_USE)
				.balance(10000L)
				.accountNumber("1000000000")
				.build();

		Transaction transaction = Transaction.builder()
				.account(account)
				.transactionType(TransactionType.USE)
				.transactionResultType(TransactionResultType.S)
				.transactionId("transactionId")
				.transactedAt(LocalDateTime.now().minusYears(1).minusDays(1))
				.amount(CANCEL_AMOUNT)
				.balanceSnapshot(10000L)
				.build();
		given(transactionRepository.findByTransactionId(anyString()))
			.willReturn(Optional.of(transaction));
		given(accountRepository.findByAccountNumber(anyString()))
			.willReturn(Optional.of(account));
		
		//when
		AccountException accountException = assertThrows(AccountException.class,
				() -> transactionService.cancelBalance("transactionId","1000000000",CANCEL_AMOUNT));
				
		//then
		assertEquals(ErrorCode.TOO_OLD_ORDER_TO_CANCEL, accountException.getErrorCode());
	}
	
	@Test
	void successQueryTransaction(){
		//given
		AccountUser user = AccountUser.builder()
				.id(12L)
				.name("pobi")
				.build();
		
		Account account = Account.builder().
				id(1L)
				.accountUser(user)
				.accountStatus(AccountStatus.IN_USE)
				.balance(10000L)
				.accountNumber("1000000000")
				.build();

		Transaction transaction = Transaction.builder()
				.account(account)
				.transactionType(TransactionType.USE)
				.transactionResultType(TransactionResultType.S)
				.transactionId("transactionId")
				.transactedAt(LocalDateTime.now().minusYears(1).minusDays(1))
				.amount(CANCEL_AMOUNT)
				.balanceSnapshot(10000L)
				.build();
		given(transactionRepository.findByTransactionId(anyString()))
			.willReturn(Optional.of(transaction));
		
		//when
		TransactionDto transactionDto = transactionService.queryTransaction("trxId");
		
		//then
		assertEquals(TransactionType.USE,transactionDto.getTransactionType());
		assertEquals(TransactionResultType.S,transactionDto.getTransactionResultType());
		assertEquals(CANCEL_AMOUNT,transactionDto.getAmount());
		assertEquals("transactionId",transactionDto.getTransactionId());
	}
	
	@Test
	@DisplayName("원 거래 없음 - 거래 조회 실패")
	void queryTransaction_TransactionNotFound(){
		//given
		given(transactionRepository.findByTransactionId(anyString()))
			.willReturn(Optional.empty());
		
		//when
		AccountException accountException = assertThrows(AccountException.class,
				() -> transactionService.queryTransaction("transactionId"));
				
		//then
		assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, accountException.getErrorCode());
	}

}

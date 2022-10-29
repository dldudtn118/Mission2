package com.example.account.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // 오토와이어드를 필드대신 생성자로 할건데 양이 많을때 대체 겟셋이랑비슷
							// final이 붙은것만 생성자로 만들어줌 String st; 이런건 안해줌
							// 빈에 등록안하고 final하면 변수가 초기화안됫다고 에러가남
public class AccountService {

	private final AccountRepository accountRepository;
	private final AccountUserRepository accountUserRepository;

	/**
	 * 슬러시별별 엔터누르면 자동생성 사용자가 있는지 조회 계좌 번호 생성 계좌 번호 저장, 그정보를 넘긴다.
	 */

	@Transactional 
	public AccountDto createAccount(Long userId, Long initialBalance) {
		// Optional 클래스를 이용하여 null값에대한 고려를 지웟다
		AccountUser accountUser = accountUserRepository.findById(userId)
				.orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));
		
		validateCreateAccount(accountUser);//계좌 10개면 더이상 생성못하게 예외처리

		String newAccountNumber = accountRepository.findFirstByOrderByIdDesc()
				.map(account -> (Integer.parseInt(account.getAccountNumber())) + 1 + "").orElse("1000000000");

		Account account = accountRepository.save(Account.builder()
				.accountUser(accountUser)
				.accountStatus(AccountStatus.IN_USE)
				.accountNumber(newAccountNumber)
				.balance(initialBalance)
				.registeredAt(LocalDateTime.now())
				.build());

		return AccountDto.fromEntity(account);
	}
	
	private void validateCreateAccount(AccountUser accountUser) {
		if(accountRepository.countByAccountUser(accountUser) == 10) {
			throw new AccountException(ErrorCode.MAX_ACCOUNT_PER_USER_10);
		}
	}

	@Transactional
	public Account getAccount(Long id) {
		if (id < 0) {
			throw new RuntimeException("Minus");
		}
		return accountRepository.findById(id).get();
	}
	
	@Transactional
	public AccountDto deleteAccount(Long userId, String accountNumber) {
		AccountUser accountUser = accountUserRepository.findById(userId)
				.orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));
		Account account = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUNT));
		
		validateCreateAccount(accountUser,account);
		
		account.setAccountStatus(AccountStatus.UN_REGISTERED);
		account.setUnRegisteredAt(LocalDateTime.now());
		
		accountRepository.save(account);//필요없는 코드인데 테스트를위해 씀 굳이 작성할필요x
		
		return AccountDto.fromEntity(account);
	}
	
	private void validateCreateAccount(AccountUser accountUser, Account account) {
		if(accountUser.getId() != account.getAccountUser().getId()) {
			throw new AccountException(ErrorCode.USER_ACCOUNT_UN_MATCH);
		}
		if(account.getAccountStatus() == AccountStatus.UN_REGISTERED) {
			throw new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
		}
		if(account.getBalance() > 0) {
			throw new AccountException(ErrorCode.BALANCE_NOT_EMPTY);
		}
		
	}
	
	@Transactional
	public List<AccountDto> getAccountByUserId(Long userId) {
		AccountUser accountUser = accountUserRepository.findById(userId)
				.orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));
		
		List<Account> accounts = accountRepository.findByAccountUser(accountUser);
		
		return accounts.stream().map(AccountDto::fromEntity)
				.collect(Collectors.toList());
	}
}

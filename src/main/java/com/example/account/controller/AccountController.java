package com.example.account.controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.domain.Account;
import com.example.account.dto.AccountDto;
import com.example.account.dto.AccountInfo;
import com.example.account.dto.CreateAccount;
import com.example.account.dto.DeleteAccount;
import com.example.account.service.AccountService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AccountController {
	private final AccountService accountService;
		
	@PostMapping("/account")
	public CreateAccount.Response createAccount(//@Valid는 유효성검사 dto에 검사법작성
			@RequestBody @Valid CreateAccount.Request request) {
		
		AccountDto accountDto = accountService.createAccount(
				request.getUserId(), request.getInitialBalance());
		
		return CreateAccount.Response.from(accountDto);
	}
	
	@DeleteMapping("/account")
	public DeleteAccount.Response deleteAccount(//@Valid는 유효성검사 dto에 검사법작성
			@RequestBody @Valid DeleteAccount.Request request) {
		
		AccountDto accountDto = accountService.deleteAccount(
				request.getUserId(), request.getAccountNumber());
		
		return DeleteAccount.Response.from(accountDto);
	}
	
	@GetMapping("/account")
	public List<AccountInfo> getAccountsByUserId(
			@RequestParam("user_id") Long userId){
			
		return accountService.getAccountByUserId(userId)
				.stream().map(accountDto -> AccountInfo.builder()
						.accountNumber(accountDto.getAccountNumber())
						.balance(accountDto.getBalance())
						.build())
				.collect(Collectors.toList());
	}
	
	
	@GetMapping("/account/{id}")
	public Account getAccount(@PathVariable Long id) {
		return accountService.getAccount(id);
	}
}

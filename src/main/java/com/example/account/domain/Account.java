package com.example.account.domain;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.account.exception.AccountException;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)//날짜 자동생성하기위한 어노테이션 컨피그(Jpa config)에도 설정해야함.
public class Account { //h2디비 테이블생성
	@Id //pk로 쓰겟다
	@GeneratedValue
	private Long id;
	
	@ManyToOne
	private AccountUser accountUser;
	private String accountNumber;
	
	@Enumerated(EnumType.STRING)//열거형은 0~3~~인데 String형으로 쓰겟다는의미
	private AccountStatus accountStatus;
	private Long balance;

	private LocalDateTime registeredAt;
	private LocalDateTime unRegisteredAt;
	
	@CreatedDate
	private LocalDateTime createdAt;
	@LastModifiedDate
	private LocalDateTime updateAt;
	
	public void useBalance(Long amount) {
		if(amount > balance) {
			throw new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);
		}
		balance -= amount;
	}
	
	public void cancelBalance(Long amount) {
		if(amount < 0) {
			throw new AccountException(ErrorCode.INVALID_REQUEST);
		}
		balance += amount;
	}
}

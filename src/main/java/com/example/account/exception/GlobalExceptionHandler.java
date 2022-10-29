package com.example.account.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.account.dto.ErrorResponse;
import com.example.account.type.ErrorCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(AccountException.class)
	public ErrorResponse HandleAccountException(AccountException e) {
		log.error("{} is occured.", e.getErrorCode());
		
		return new ErrorResponse(e.getErrorCode(),e.getErrorMessage());
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ErrorResponse HandleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		log.error("MethodArgumentNotValidException is occured.", e);
		
		return new ErrorResponse(ErrorCode.INVALID_REQUEST,
				ErrorCode.INVALID_REQUEST.getDescription());
	}
	
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ErrorResponse HandleDataIntegrityViolationException(DataIntegrityViolationException e) {
		log.error("DataIntegrityViolationException is occured.", e);
		
		return new ErrorResponse(ErrorCode.INVALID_REQUEST,
				ErrorCode.INVALID_REQUEST.getDescription());
	}
	
	@ExceptionHandler(Exception.class)
	public ErrorResponse HandleException(Exception e) {
		log.error("Exception is occured.", e);
		
		return new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR,
				ErrorCode.INTERNAL_SERVER_ERROR.getDescription());
	}
}

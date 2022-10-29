package com.example.account.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)//어노테이션 붙일수있게해줌
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface AccountLock {//어노테이션
	long tryLockTIme() default 5000L;
}

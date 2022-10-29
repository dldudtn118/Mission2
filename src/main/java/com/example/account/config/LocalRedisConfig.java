package com.example.account.config;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import redis.embedded.RedisServer;


@Configuration
public class LocalRedisConfig {
	@Value("${spring.redis.port}") //yml에 있는 redis 가져와서 담겟다.
	private int redisPort;
	
	private RedisServer redisServer;
	
	@PostConstruct
	public void startRedis() {
		redisServer = new RedisServer(redisPort);
		redisServer.start();
	}
	
	@PreDestroy
	public void stopRedis() {
		if(redisServer != null) {
			redisServer.stop();
		}
	}

}

package com.polarbookshop.edgeservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest( //Loads a full Spring web application context and a web environment listening on a random port
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Testcontainers //Activates automatic startup and cleanup of test containers
class EdgeServiceApplicationTests {

	private static final int REDIS_PORT = 6379;

	@Container
	static GenericContainer<?> redis =  //Defines a Redis container for testing
			new GenericContainer<>(DockerImageName.parse("redis:7.0"))
			.withExposedPorts(REDIS_PORT);

	@DynamicPropertySource //Overwrites the Redis configuration to point to the test Redis instance
	static void redisProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.redis.host", () -> redis.getHost());
		registry.add("spring.redis.port", () -> redis.getMappedPort(REDIS_PORT));
	}

	@Test
	void verifyThatSpringContextLoads() { //An empty test used to verify that the application context is loaded correctly and
											// that a connection with Redis has been established successfully
	}

}

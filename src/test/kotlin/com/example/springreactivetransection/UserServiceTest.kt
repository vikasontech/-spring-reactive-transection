package com.example.springreactivetransection

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.test.StepVerifier

@SpringBootTest
internal class UserServiceTest {
  @Autowired
  private lateinit var userRepo: UserRepo

  @Autowired
  private lateinit var customerService: UserService

  @Test
  fun saveAll(): Unit {
    StepVerifier.create(userRepo.deleteAll())
        .expectSubscription()
        .verifyComplete()

    StepVerifier.create(customerService
        .saveAll(ages= *intArrayOf(10, 11, 5)).log("step#2:::"))
        .verifyError()

    StepVerifier.create(
        userRepo.findAll().log("step#3:::"))
        .expectSubscription()
        .expectNextCount(0)
        .verifyComplete()

    StepVerifier.create(
        customerService
            .saveAll(ages= *intArrayOf(18, 10, 20)).log("step4:::"))
        .expectError()
        .verify()

    StepVerifier.create(
        userRepo.findAll().log("step5:::"))
        .expectSubscription()
        .expectNextCount(0)
        .verifyComplete()
  }

}
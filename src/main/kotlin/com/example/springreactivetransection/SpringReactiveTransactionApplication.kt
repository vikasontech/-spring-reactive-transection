package com.example.springreactivetransection

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.ReactiveMongoTransactionManager
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.util.Assert
import reactor.core.publisher.Flux
import java.util.*

@SpringBootApplication
class SpringReactiveTransactionApplication {

  @Bean
  fun transactionManager(rdbf: ReactiveMongoDatabaseFactory): ReactiveMongoTransactionManager {
    return ReactiveMongoTransactionManager(rdbf)
  }

  @Bean
  fun transactionOperator(rtm: ReactiveTransactionManager): TransactionalOperator {
    return TransactionalOperator.create(rtm)
  }

}

fun main(args: Array<String>) {
  runApplication<SpringReactiveTransactionApplication>(*args)
}

@Document
data class User(
    @Id
    val id: String,
    val age: Int
)

interface UserRepo : ReactiveCrudRepository<User, String>

@Service
class UserService(val userRepo: UserRepo,
                  val transactionalOperator: TransactionalOperator) {

  @Transactional
  fun saveAll(vararg ages: Int): Flux<User> {
    val data = Flux.fromIterable(ages.asIterable())
        .map {
          User(id = UUID.randomUUID().toString(),
              age = it)
        }
        .doOnNext { Assert.isTrue(it.age >= 18, "Age should be greater or equal to 18") }
        .flatMap { userRepo.save(it) }

    return transactionalOperator.execute { data }
  }

}


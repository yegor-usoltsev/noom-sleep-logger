package com.noom.interview.fullstack.sleep.repository

import com.noom.interview.fullstack.sleep.IntegrationTest
import com.noom.interview.fullstack.sleep.jooq.enums.Mood
import com.noom.interview.fullstack.sleep.model.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.model.CreateUserRequest
import org.assertj.core.api.Assertions.*
import org.assertj.core.api.ThrowableAssert.ThrowingCallable
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

class SleepLogRepositoryTest @Autowired constructor(
  private val sleepLogRepository: SleepLogRepository,
  private val userRepository: UserRepository
) : IntegrationTest() {

  @Test
  fun `create should create a new sleep log`() {
    // Given
    val user = userRepository.create(CreateUserRequest(name = "test-user"))
    val newSleepLog = CreateSleepLogRequest(
      bedTime = Instant.now().minus(8, ChronoUnit.HOURS),
      wakeTime = Instant.now(),
      mood = Mood.GOOD
    )

    // When
    val createdSleepLog = sleepLogRepository.create(user.id, newSleepLog)

    // Then
    assertThat(createdSleepLog.id).isNotNull()
    assertThat(createdSleepLog.userId).isEqualTo(user.id)
    assertThat(createdSleepLog.bedTime).isCloseTo(newSleepLog.bedTime, within(1, ChronoUnit.SECONDS))
    assertThat(createdSleepLog.wakeTime).isCloseTo(newSleepLog.wakeTime, within(1, ChronoUnit.SECONDS))
    assertThat(createdSleepLog.mood).isEqualTo(newSleepLog.mood)
    assertThat(createdSleepLog.date).isEqualTo(newSleepLog.wakeTime.atOffset(ZoneOffset.UTC).toLocalDate())
    assertThat(createdSleepLog.duration).isEqualTo(Duration.between(newSleepLog.bedTime, newSleepLog.wakeTime))
    assertThat(createdSleepLog.createdAt).isNotNull()
    assertThat(createdSleepLog.updatedAt).isNotNull()
  }

  @Test
  fun `create should throw when user does not exist`() {
    // Given
    val nonExistentUserId = UUID.randomUUID()
    val newSleepLog = CreateSleepLogRequest(
      bedTime = Instant.now().minus(8, ChronoUnit.HOURS),
      wakeTime = Instant.now(),
      mood = Mood.GOOD
    )

    // When
    val code = ThrowingCallable { sleepLogRepository.create(nonExistentUserId, newSleepLog) }

    // Then
    assertThatThrownBy(code).isInstanceOf(DataIntegrityViolationException::class.java)
  }

  @Test
  fun `create should throw when sleep log already exists for the same user and date`() {
    // Given
    val user = userRepository.create(CreateUserRequest(name = "test-user"))
    val newSleepLog = CreateSleepLogRequest(
      bedTime = Instant.now().minus(8, ChronoUnit.HOURS),
      wakeTime = Instant.now(),
      mood = Mood.GOOD
    )
    sleepLogRepository.create(user.id, newSleepLog)

    // When
    val code = ThrowingCallable { sleepLogRepository.create(user.id, newSleepLog) }

    // Then
    assertThatThrownBy(code).isInstanceOf(DuplicateKeyException::class.java)
  }

  @Test
  fun `create should throw when wake time is before bed time`() {
    // Given
    val user = userRepository.create(CreateUserRequest(name = "test-user"))
    val newSleepLog = CreateSleepLogRequest(
      bedTime = Instant.now(),
      wakeTime = Instant.now().minus(8, ChronoUnit.HOURS),
      mood = Mood.GOOD
    )

    // When
    val code = ThrowingCallable { sleepLogRepository.create(user.id, newSleepLog) }

    // Then
    assertThatThrownBy(code).isInstanceOf(DataIntegrityViolationException::class.java)
  }

}

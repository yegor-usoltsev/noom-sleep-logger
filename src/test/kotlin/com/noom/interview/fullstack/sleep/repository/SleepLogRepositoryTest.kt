package com.noom.interview.fullstack.sleep.repository

import com.noom.interview.fullstack.sleep.jooq.enums.Mood
import com.noom.interview.fullstack.sleep.model.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.model.CreateUserRequest
import org.assertj.core.api.Assertions.*
import org.assertj.core.api.ThrowableAssert.ThrowingCallable
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jooq.JooqTest
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

@JooqTest
@Import(SleepLogRepository::class, UserRepository::class)
class SleepLogRepositoryTest @Autowired constructor(
  private val sleepLogRepository: SleepLogRepository,
  private val userRepository: UserRepository
) {

  @Test
  fun `create should create a new sleep log`() {
    // Given
    val user = userRepository.create(CreateUserRequest(name = "test-user"))
    val given = CreateSleepLogRequest(
      bedTime = Instant.now().minus(8, ChronoUnit.HOURS),
      wakeTime = Instant.now(),
      mood = Mood.GOOD
    )

    // When
    val actual = sleepLogRepository.create(user.id, given)

    // Then
    assertThat(actual.id).isNotNull()
    assertThat(actual.userId).isEqualTo(user.id)
    assertThat(actual.bedTime).isCloseTo(given.bedTime, within(1, ChronoUnit.SECONDS))
    assertThat(actual.wakeTime).isCloseTo(given.wakeTime, within(1, ChronoUnit.SECONDS))
    assertThat(actual.mood).isEqualTo(given.mood)
    assertThat(actual.date).isEqualTo(given.wakeTime.atOffset(ZoneOffset.UTC).toLocalDate())
    assertThat(actual.duration).isCloseTo(Duration.between(given.bedTime, given.wakeTime), Duration.ofSeconds(1))
    assertThat(actual.createdAt).isNotNull()
    assertThat(actual.updatedAt).isNotNull()
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

  @Test
  fun `findAll should return all sleep logs for a user`() {
    // Given
    val user = userRepository.create(CreateUserRequest(name = "test-user"))
    val sleepLog1 = sleepLogRepository.create(
      user.id,
      CreateSleepLogRequest(
        bedTime = Instant.now().minus(24 + 8, ChronoUnit.HOURS),
        wakeTime = Instant.now().minus(24, ChronoUnit.HOURS),
        mood = Mood.GOOD
      )
    )
    val sleepLog2 = sleepLogRepository.create(
      user.id,
      CreateSleepLogRequest(
        bedTime = Instant.now().minus(6, ChronoUnit.HOURS),
        wakeTime = Instant.now(),
        mood = Mood.OK
      )
    )

    // When
    val sleepLogs = sleepLogRepository.findAll(user.id)

    // Then
    assertThat(sleepLogs).hasSizeGreaterThanOrEqualTo(2)
    assertThat(sleepLogs).contains(sleepLog1, sleepLog2)
  }

  @Test
  fun `findLatest should return the most recent sleep log for a user`() {
    // Given
    val user = userRepository.create(CreateUserRequest(name = "test-user"))
    sleepLogRepository.create(
      user.id,
      CreateSleepLogRequest(
        bedTime = Instant.now().minus(24 + 8, ChronoUnit.HOURS),
        wakeTime = Instant.now().minus(24, ChronoUnit.HOURS),
        mood = Mood.GOOD
      )
    )
    val latestLog = sleepLogRepository.create(
      user.id,
      CreateSleepLogRequest(
        bedTime = Instant.now().minus(6, ChronoUnit.HOURS),
        wakeTime = Instant.now(),
        mood = Mood.OK
      )
    )

    // When
    val foundLog = sleepLogRepository.findLatest(user.id)

    // Then
    assertThat(foundLog).isNotNull()
    assertThat(foundLog).isEqualTo(latestLog)
  }


  @Test
  fun `findLatest should return null when sleep log does not exist`() {
    // Given
    val userId = UUID.randomUUID()
    val nonExistentId = UUID.randomUUID()

    // When
    val foundLog = sleepLogRepository.findLatest(userId)

    // Then
    assertThat(foundLog).isNull()
  }

  @Test
  fun `findById should return sleep log when exists`() {
    // Given
    val user = userRepository.create(CreateUserRequest(name = "test-user"))
    val createdLog = sleepLogRepository.create(
      user.id,
      CreateSleepLogRequest(
        bedTime = Instant.now().minus(6, ChronoUnit.HOURS),
        wakeTime = Instant.now(),
        mood = Mood.OK
      )
    )

    // When
    val foundLog = sleepLogRepository.findById(user.id, createdLog.id)

    // Then
    assertThat(foundLog).isNotNull()
    assertThat(foundLog).isEqualTo(createdLog)
  }

  @Test
  fun `findById should return null when sleep log does not exist`() {
    // Given
    val userId = UUID.randomUUID()
    val nonExistentId = UUID.randomUUID()

    // When
    val foundLog = sleepLogRepository.findById(userId, nonExistentId)

    // Then
    assertThat(foundLog).isNull()
  }

  @Test
  fun `findById should return null when sleep log belongs to different user`() {
    // Given
    val user1 = userRepository.create(CreateUserRequest(name = "test-user-1"))
    val user2 = userRepository.create(CreateUserRequest(name = "test-user-2"))
    val createdLogForUser2 = sleepLogRepository.create(
      user2.id,
      CreateSleepLogRequest(
        bedTime = Instant.now().minus(6, ChronoUnit.HOURS),
        wakeTime = Instant.now(),
        mood = Mood.OK
      )
    )

    // When
    val foundLog = sleepLogRepository.findById(user1.id, createdLogForUser2.id)

    // Then
    assertThat(foundLog).isNull()
  }

}

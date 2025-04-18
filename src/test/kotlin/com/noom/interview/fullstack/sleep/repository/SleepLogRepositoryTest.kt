package com.noom.interview.fullstack.sleep.repository

import com.noom.interview.fullstack.sleep.createSleepLogRequest
import com.noom.interview.fullstack.sleep.createUserRequest
import com.noom.interview.fullstack.sleep.jooq.enums.Mood
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
    val user = userRepository.create(createUserRequest())
    val given = createSleepLogRequest()

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
    val newSleepLog = createSleepLogRequest()

    // When
    val code = ThrowingCallable { sleepLogRepository.create(nonExistentUserId, newSleepLog) }

    // Then
    assertThatThrownBy(code).isInstanceOf(DataIntegrityViolationException::class.java)
  }

  @Test
  fun `create should throw when sleep log already exists for the same user and date`() {
    // Given
    val user = userRepository.create(createUserRequest())
    val newSleepLog = createSleepLogRequest()
    sleepLogRepository.create(user.id, newSleepLog)

    // When
    val code = ThrowingCallable { sleepLogRepository.create(user.id, newSleepLog) }

    // Then
    assertThatThrownBy(code).isInstanceOf(DuplicateKeyException::class.java)
  }

  @Test
  fun `create should throw when when wakeTime is before bedTime`() {
    // Given
    val user = userRepository.create(createUserRequest())
    val newSleepLog = createSleepLogRequest(
      bedTime = Instant.now(),
      wakeTime = Instant.now().minus(8, ChronoUnit.HOURS) // wakeTime is before bedTime
    )

    // When
    val code = ThrowingCallable { sleepLogRepository.create(user.id, newSleepLog) }

    // Then
    assertThatThrownBy(code).isInstanceOf(DataIntegrityViolationException::class.java)
  }

  @Test
  fun `findAll should return all sleep logs for a user in order`() {
    // Given
    val user = userRepository.create(createUserRequest())
    val sleepLog1 = sleepLogRepository.create(
      user.id,
      createSleepLogRequest(
        bedTime = Instant.now().minus(24 + 8, ChronoUnit.HOURS),
        wakeTime = Instant.now().minus(24, ChronoUnit.HOURS)
      )
    )
    val sleepLog2 = sleepLogRepository.create(user.id, createSleepLogRequest())

    // When
    val sleepLogs = sleepLogRepository.findAll(user.id)

    // Then
    assertThat(sleepLogs).hasSize(2)
    assertThat(sleepLogs).containsExactly(sleepLog2, sleepLog1)
  }

  @Test
  fun `findLatest should return the most recent sleep log for a user`() {
    // Given
    val user = userRepository.create(createUserRequest())
    sleepLogRepository.create(
      user.id,
      createSleepLogRequest(
        bedTime = Instant.now().minus(24 + 8, ChronoUnit.HOURS),
        wakeTime = Instant.now().minus(24, ChronoUnit.HOURS)
      )
    )
    val latestLog = sleepLogRepository.create(user.id, createSleepLogRequest())

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

    // When
    val foundLog = sleepLogRepository.findLatest(userId)

    // Then
    assertThat(foundLog).isNull()
  }

  @Test
  fun `findById should return sleep log when exists`() {
    // Given
    val user = userRepository.create(createUserRequest())
    val createdLog = sleepLogRepository.create(user.id, createSleepLogRequest())

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
    val user1 = userRepository.create(createUserRequest())
    val user2 = userRepository.create(createUserRequest())
    val createdLogForUser2 = sleepLogRepository.create(user2.id, createSleepLogRequest())

    // When
    val foundLog = sleepLogRepository.findById(user1.id, createdLogForUser2.id)

    // Then
    assertThat(foundLog).isNull()
  }

  @Test
  fun `updateById should update sleep log when exists`() {
    // Given
    val user = userRepository.create(createUserRequest())
    val createdLog = sleepLogRepository.create(user.id, createSleepLogRequest())
    val updateRequest = createSleepLogRequest(
      bedTime = createdLog.bedTime.minus(1, ChronoUnit.HOURS),
      wakeTime = createdLog.wakeTime.minus(1, ChronoUnit.HOURS),
      mood = Mood.entries.filter { it != createdLog.mood }.random()
    )

    // When
    val updatedLog = sleepLogRepository.updateById(user.id, createdLog.id, updateRequest)

    // Then
    assertThat(updatedLog).isNotNull()
    assertThat(updatedLog!!.id).isEqualTo(createdLog.id)
    assertThat(updatedLog.userId).isEqualTo(createdLog.userId)
    assertThat(updatedLog.bedTime).isCloseTo(updateRequest.bedTime, within(1, ChronoUnit.SECONDS))
    assertThat(updatedLog.wakeTime).isCloseTo(updateRequest.wakeTime, within(1, ChronoUnit.SECONDS))
    assertThat(updatedLog.mood).isEqualTo(updateRequest.mood)
    assertThat(updatedLog.date).isEqualTo(updateRequest.wakeTime.atOffset(ZoneOffset.UTC).toLocalDate())
    assertThat(updatedLog.duration).isCloseTo(
      Duration.between(updateRequest.bedTime, updateRequest.wakeTime),
      Duration.ofSeconds(1)
    )
    assertThat(updatedLog.createdAt).isEqualTo(createdLog.createdAt)
    assertThat(updatedLog.updatedAt).isAfter(createdLog.updatedAt)
  }

  @Test
  fun `updateById should return null when sleep log does not exist`() {
    // Given
    val userId = UUID.randomUUID()
    val nonExistentId = UUID.randomUUID()
    val updateRequest = createSleepLogRequest()

    // When
    val updatedLog = sleepLogRepository.updateById(userId, nonExistentId, updateRequest)

    // Then
    assertThat(updatedLog).isNull()
  }

  @Test
  fun `updateById should return null when sleep log belongs to different user`() {
    // Given
    val user1 = userRepository.create(createUserRequest())
    val user2 = userRepository.create(createUserRequest())
    val createdLogForUser2 = sleepLogRepository.create(user2.id, createSleepLogRequest())
    val updateRequest = createSleepLogRequest()

    // When
    val updatedLog = sleepLogRepository.updateById(user1.id, createdLogForUser2.id, updateRequest)

    // Then
    assertThat(updatedLog).isNull()
  }

  @Test
  fun `deleteById should delete sleep log when exists`() {
    // Given
    val user = userRepository.create(createUserRequest())
    val createdLog = sleepLogRepository.create(user.id, createSleepLogRequest())

    // When
    val deletedLog = sleepLogRepository.deleteById(user.id, createdLog.id)

    // Then
    assertThat(deletedLog).isNotNull()
    assertThat(deletedLog).isEqualTo(createdLog)
    assertThat(sleepLogRepository.findById(user.id, createdLog.id)).isNull()
  }

  @Test
  fun `deleteById should return null when sleep log does not exist`() {
    // Given
    val userId = UUID.randomUUID()
    val nonExistentId = UUID.randomUUID()

    // When
    val deletedLog = sleepLogRepository.deleteById(userId, nonExistentId)

    // Then
    assertThat(deletedLog).isNull()
  }

  @Test
  fun `deleteById should return null when sleep log belongs to different user`() {
    // Given
    val user1 = userRepository.create(createUserRequest())
    val user2 = userRepository.create(createUserRequest())
    val createdLogForUser2 = sleepLogRepository.create(user2.id, createSleepLogRequest())

    // When
    val deletedLog = sleepLogRepository.deleteById(user1.id, createdLogForUser2.id)

    // Then
    assertThat(deletedLog).isNull()
    assertThat(sleepLogRepository.findById(user2.id, createdLogForUser2.id)).isNotNull()
  }

}

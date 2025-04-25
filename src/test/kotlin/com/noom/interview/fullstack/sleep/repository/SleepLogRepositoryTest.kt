package com.noom.interview.fullstack.sleep.repository

import com.noom.interview.fullstack.sleep.*
import com.noom.interview.fullstack.sleep.jooq.enums.Mood
import com.noom.interview.fullstack.sleep.model.MoodFrequencies
import com.noom.interview.fullstack.sleep.model.Pagination
import org.assertj.core.api.Assertions.*
import org.assertj.core.api.ThrowableAssert.ThrowingCallable
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jooq.JooqTest
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
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
    val user = userRepository.create(createUserRequest(timeZone = LAX))
    val given = createSleepLogRequest(
      bedTime = ZonedDateTime.now(LAX).minusHours(8),
      wakeTime = ZonedDateTime.now(LAX)
    )

    // When
    val actual = sleepLogRepository.create(user.id, given)

    // Then
    assertThat(actual.id).isNotNull()
    assertThat(actual.userId).isEqualTo(user.id)
    assertThat(actual.bedTime).isCloseTo(given.bedTime, within(1, ChronoUnit.SECONDS))
    assertThat(actual.wakeTime).isCloseTo(given.wakeTime, within(1, ChronoUnit.SECONDS))
    assertThat(actual.mood).isEqualTo(given.mood)
    assertThat(actual.date).isEqualTo(given.wakeTime.toLocalDate())
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
      bedTime = ZonedDateTime.now(UTC),
      wakeTime = ZonedDateTime.now(UTC).minusHours(8) // wakeTime is before bedTime
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
    sleepLogRepository.create(
      user.id,
      createSleepLogRequest(
        bedTime = ZonedDateTime.now(LAX).minusHours(48 + 8),
        wakeTime = ZonedDateTime.now(LAX).minusHours(48)
      )
    )
    val sleepLog1 = sleepLogRepository.create(
      user.id,
      createSleepLogRequest(
        bedTime = ZonedDateTime.now(WAW).minusHours(24 + 8),
        wakeTime = ZonedDateTime.now(WAW).minusHours(24)
      )
    )
    val sleepLog2 = sleepLogRepository.create(user.id, createSleepLogRequest())
    val pagination = Pagination(limit = 2, offset = 0)

    // When
    val sleepLogs = sleepLogRepository.findAll(user.id, pagination)

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
        bedTime = ZonedDateTime.now(UTC).minusHours(24 + 8),
        wakeTime = ZonedDateTime.now(UTC).minusHours(24)
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
    val user = userRepository.create(createUserRequest(timeZone = LAX))
    val createdLog = sleepLogRepository.create(
      user.id, createSleepLogRequest(
        bedTime = ZonedDateTime.now(LAX).minusHours(8),
        wakeTime = ZonedDateTime.now(LAX)
      )
    )
    val updateRequest = createSleepLogRequest(
      bedTime = createdLog.bedTime.minusMinutes(1),
      wakeTime = createdLog.wakeTime.plusMinutes(1),
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
    assertThat(updatedLog.date).isEqualTo(updateRequest.wakeTime.toLocalDate())
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
    val deleted = sleepLogRepository.deleteById(user.id, createdLog.id)

    // Then
    assertThat(deleted).isTrue()
    assertThat(sleepLogRepository.findById(user.id, createdLog.id)).isNull()
  }

  @Test
  fun `deleteById should return null when sleep log does not exist`() {
    // Given
    val userId = UUID.randomUUID()
    val nonExistentId = UUID.randomUUID()

    // When
    val deleted = sleepLogRepository.deleteById(userId, nonExistentId)

    // Then
    assertThat(deleted).isFalse()
  }

  @Test
  fun `deleteById should return null when sleep log belongs to different user`() {
    // Given
    val user1 = userRepository.create(createUserRequest())
    val user2 = userRepository.create(createUserRequest())
    val createdLogForUser2 = sleepLogRepository.create(user2.id, createSleepLogRequest())

    // When
    val deleted = sleepLogRepository.deleteById(user1.id, createdLogForUser2.id)

    // Then
    assertThat(deleted).isFalse()
    assertThat(sleepLogRepository.findById(user2.id, createdLogForUser2.id)).isNotNull()
  }

  @Test
  fun `calculateSleepStats should return sleep stats for a user`() {
    // Given
    val user = userRepository.create(createUserRequest(timeZone = LAX))
    val nowDate = LocalDate.now(user.timeZone)
    val now = nowDate.atStartOfDay(user.timeZone).plusHours(7).plusMinutes(30) // 07:30
    val daysBack = 30
    sleepLogRepository.create(
      user.id,
      createSleepLogRequest(
        bedTime = now.minusHours(24 + 8), // 23:30
        wakeTime = now.minusHours(24), // 07:30
        mood = Mood.GOOD
      )
    )
    sleepLogRepository.create(
      user.id,
      createSleepLogRequest(
        bedTime = now.minusHours(48 + 7), // 00:30
        wakeTime = now.minusHours(48 - 1), // 08:30
        mood = Mood.OK
      )
    )
    sleepLogRepository.create(
      user.id,
      createSleepLogRequest(
        bedTime = now.minusHours(72 + 6), // 01:30
        wakeTime = now.minusHours(72 - 2), // 09:30
        mood = Mood.BAD
      )
    )

    // When
    val stats = sleepLogRepository.calculateSleepStats(user.id, daysBack)

    // Then
    assertThat(stats).isNotNull()
    assertThat(stats!!.userId).isEqualTo(user.id)
    assertThat(stats.timeZone).isEqualTo(user.timeZone)
    assertThat(stats.fromDate).isEqualTo(nowDate.minusDays(daysBack.toLong()))
    assertThat(stats.toDate).isEqualTo(nowDate)
    assertThat(stats.averageBedTime).isCloseTo(LocalTime.of(0, 30), within(1, ChronoUnit.SECONDS))
    assertThat(stats.averageWakeTime).isCloseTo(LocalTime.of(8, 30), within(1, ChronoUnit.SECONDS))
    assertThat(stats.averageDuration).isCloseTo(Duration.ofHours(8), Duration.ofSeconds(1))
    assertThat(stats.moodFrequencies).isEqualTo(MoodFrequencies(bad = 1, ok = 1, good = 1))
  }

  @Test
  fun `calculateSleepStats should return null when no sleep logs exist`() {
    // Given
    val userId = UUID.randomUUID()
    val daysBack = 30

    // When
    val stats = sleepLogRepository.calculateSleepStats(userId, daysBack)

    // Then
    assertThat(stats).isNull()
  }

}

package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.jooq.enums.Mood
import com.noom.interview.fullstack.sleep.model.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.repository.SleepLogRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

class SleepLogServiceTest {

  private val sleepLogRepository = mockk<SleepLogRepository>()
  private val sleepLogService = SleepLogService(sleepLogRepository)

  @Test
  fun create() {
    // Given
    val userId = UUID.randomUUID()
    val request = CreateSleepLogRequest(
      bedTime = Instant.now().minus(8, ChronoUnit.HOURS),
      wakeTime = Instant.now(),
      mood = Mood.GOOD
    )
    val expectedSleepLog = SleepLog(
      id = UUID.randomUUID(),
      userId = userId,
      bedTime = request.bedTime,
      wakeTime = request.wakeTime,
      mood = request.mood,
      date = request.wakeTime.atOffset(ZoneOffset.UTC).toLocalDate(),
      duration = Duration.between(request.bedTime, request.wakeTime),
      createdAt = Instant.now(),
      updatedAt = Instant.now()
    )
    every { sleepLogRepository.create(userId, request) } returns expectedSleepLog

    // When
    val result = sleepLogService.create(userId, request)

    // Then
    assertThat(result).isEqualTo(expectedSleepLog)
    verify(exactly = 1) { sleepLogRepository.create(userId, request) }
  }

  @Test
  fun findAll() {
    // Given
    val userId = UUID.randomUUID()
    val expectedSleepLogs = listOf(
      SleepLog(
        id = UUID.randomUUID(),
        userId = userId,
        bedTime = Instant.now().minus(24 + 8, ChronoUnit.HOURS),
        wakeTime = Instant.now().minus(24, ChronoUnit.HOURS),
        mood = Mood.GOOD,
        date = LocalDate.now(),
        duration = Duration.ofHours(8),
        createdAt = Instant.now(),
        updatedAt = Instant.now()
      ),
      SleepLog(
        id = UUID.randomUUID(),
        userId = userId,
        bedTime = Instant.now().minus(6, ChronoUnit.HOURS),
        wakeTime = Instant.now(),
        mood = Mood.OK,
        date = LocalDate.now(),
        duration = Duration.ofHours(6),
        createdAt = Instant.now(),
        updatedAt = Instant.now()
      )
    )
    every { sleepLogRepository.findAll(userId) } returns expectedSleepLogs

    // When
    val result = sleepLogService.findAll(userId)

    // Then
    assertThat(result).isEqualTo(expectedSleepLogs)
    verify(exactly = 1) { sleepLogRepository.findAll(userId) }
  }

  @Test
  fun findLatest() {
    // Given
    val userId = UUID.randomUUID()
    val expectedSleepLog = SleepLog(
      id = UUID.randomUUID(),
      userId = userId,
      bedTime = Instant.now().minus(8, ChronoUnit.HOURS),
      wakeTime = Instant.now(),
      mood = Mood.GOOD,
      date = LocalDate.now(),
      duration = Duration.ofHours(8),
      createdAt = Instant.now(),
      updatedAt = Instant.now()
    )
    every { sleepLogRepository.findLatest(userId) } returns expectedSleepLog

    // When
    val result = sleepLogService.findLatest(userId)

    // Then
    assertThat(result).isEqualTo(expectedSleepLog)
    verify(exactly = 1) { sleepLogRepository.findLatest(userId) }
  }

  @Test
  fun findById() {
    // Given
    val userId = UUID.randomUUID()
    val sleepLogId = UUID.randomUUID()
    val expectedSleepLog = SleepLog(
      id = sleepLogId,
      userId = userId,
      bedTime = Instant.now().minus(8, ChronoUnit.HOURS),
      wakeTime = Instant.now(),
      mood = Mood.GOOD,
      date = LocalDate.now(),
      duration = Duration.ofHours(8),
      createdAt = Instant.now(),
      updatedAt = Instant.now()
    )
    every { sleepLogRepository.findById(userId, sleepLogId) } returns expectedSleepLog

    // When
    val result = sleepLogService.findById(userId, sleepLogId)

    // Then
    assertThat(result).isEqualTo(expectedSleepLog)
    verify(exactly = 1) { sleepLogRepository.findById(userId, sleepLogId) }
  }

}

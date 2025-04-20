package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.createSleepLog
import com.noom.interview.fullstack.sleep.createSleepLogRequest
import com.noom.interview.fullstack.sleep.createSleepStats
import com.noom.interview.fullstack.sleep.repository.SleepLogRepository
import com.noom.interview.fullstack.sleep.toSleepLog
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*

class SleepLogServiceTest {

  private val sleepLogRepository = mockk<SleepLogRepository>()
  private val sleepLogService = SleepLogService(sleepLogRepository)

  @Test
  fun create() {
    // Given
    val userId = UUID.randomUUID()
    val request = createSleepLogRequest()
    val expectedSleepLog = request.toSleepLog(userId = userId)
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
      createSleepLog(userId = userId),
      createSleepLog(userId = userId)
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
    val expectedSleepLog = createSleepLog(userId = userId)
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
    val expectedSleepLog = createSleepLog(id = sleepLogId, userId = userId)
    every { sleepLogRepository.findById(userId, sleepLogId) } returns expectedSleepLog

    // When
    val result = sleepLogService.findById(userId, sleepLogId)

    // Then
    assertThat(result).isEqualTo(expectedSleepLog)
    verify(exactly = 1) { sleepLogRepository.findById(userId, sleepLogId) }
  }

  @Test
  fun updateById() {
    // Given
    val userId = UUID.randomUUID()
    val sleepLogId = UUID.randomUUID()
    val request = createSleepLogRequest()
    val expectedSleepLog = request.toSleepLog(id = sleepLogId, userId = userId)
    every { sleepLogRepository.updateById(userId, sleepLogId, request) } returns expectedSleepLog

    // When
    val result = sleepLogService.updateById(userId, sleepLogId, request)

    // Then
    assertThat(result).isEqualTo(expectedSleepLog)
    verify(exactly = 1) { sleepLogRepository.updateById(userId, sleepLogId, request) }
  }

  @Test
  fun deleteById() {
    // Given
    val userId = UUID.randomUUID()
    val sleepLogId = UUID.randomUUID()
    every { sleepLogRepository.deleteById(userId, sleepLogId) } returns true

    // When
    val result = sleepLogService.deleteById(userId, sleepLogId)

    // Then
    assertThat(result).isTrue()
    verify(exactly = 1) { sleepLogRepository.deleteById(userId, sleepLogId) }
  }

  @Test
  fun calculateSleepStats() {
    // Given
    val userId = UUID.randomUUID()
    val daysBack = 30
    val expectedStats = createSleepStats(
      userId = userId,
      fromDate = LocalDate.now().minusDays(daysBack.toLong())
    )
    every { sleepLogRepository.calculateSleepStats(userId, daysBack) } returns expectedStats

    // When
    val result = sleepLogService.calculateSleepStats(userId, daysBack)

    // Then
    assertThat(result).isEqualTo(expectedStats)
    verify(exactly = 1) { sleepLogRepository.calculateSleepStats(userId, daysBack) }
  }

}

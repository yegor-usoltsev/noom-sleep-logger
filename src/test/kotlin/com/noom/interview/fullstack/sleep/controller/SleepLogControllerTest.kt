package com.noom.interview.fullstack.sleep.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ninjasquad.springmockk.MockkBean
import com.noom.interview.fullstack.sleep.jooq.enums.Mood
import com.noom.interview.fullstack.sleep.model.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.service.SleepLogService
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

@WebMvcTest(SleepLogController::class)
class SleepLogControllerTest @Autowired constructor(
  private val mockMvc: MockMvc,
  private val objectMapper: ObjectMapper
) {

  @MockkBean
  private lateinit var sleepLogService: SleepLogService

  @Test
  fun `create should return 201 with created sleep log`() {
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
    every { sleepLogService.create(userId, request) } returns expectedSleepLog

    // When/Then
    mockMvc.post("/api/v1/users/{user-id}/sleep-logs", userId) {
      contentType = MediaType.APPLICATION_JSON
      content = objectMapper.writeValueAsBytes(request)
    }.andExpect {
      status { isCreated() }
    }.andDo {
      handle { result ->
        val actualSleepLog = objectMapper.readValue<SleepLog>(result.response.contentAsByteArray)
        assertThat(actualSleepLog).isEqualTo(expectedSleepLog)
      }
    }
  }

  @Test
  fun `create should return 400 when bedTime is in a future`() {
    // Given
    val userId = UUID.randomUUID()
    val request = CreateSleepLogRequest(
      bedTime = Instant.now().plus(8, ChronoUnit.HOURS), // bedTime is in a future
      wakeTime = Instant.now(),
      mood = Mood.GOOD
    )

    // When/Then
    mockMvc.post("/api/v1/users/{user-id}/sleep-logs", userId) {
      contentType = MediaType.APPLICATION_JSON
      content = objectMapper.writeValueAsBytes(request)
    }.andExpect {
      status { isBadRequest() }
    }
  }

  @Test
  fun `create should return 422 when wakeTime is before bedTime`() {
    // Given
    val userId = UUID.randomUUID()
    val request = CreateSleepLogRequest(
      bedTime = Instant.now(),
      wakeTime = Instant.now().minus(8, ChronoUnit.HOURS), // wakeTime is before bedTime
      mood = Mood.GOOD
    )

    // When/Then
    mockMvc.post("/api/v1/users/{user-id}/sleep-logs", userId) {
      contentType = MediaType.APPLICATION_JSON
      content = objectMapper.writeValueAsBytes(request)
    }.andExpect {
      status { isUnprocessableEntity() }
    }
  }

  @Test
  fun `findAll should return 200 with all sleep logs`() {
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
    every { sleepLogService.findAll(userId) } returns expectedSleepLogs

    // When/Then
    mockMvc.get("/api/v1/users/{user-id}/sleep-logs", userId)
      .andExpect {
        status { isOk() }
      }.andDo {
        handle { result ->
          val actualSleepLogs = objectMapper.readValue<List<SleepLog>>(result.response.contentAsByteArray)
          assertThat(actualSleepLogs).isEqualTo(expectedSleepLogs)
        }
      }
  }

  @Test
  fun `findLatest should return 200 with latest sleep log`() {
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
    every { sleepLogService.findLatest(userId) } returns expectedSleepLog

    // When/Then
    mockMvc.get("/api/v1/users/{user-id}/sleep-logs/latest", userId)
      .andExpect {
        status { isOk() }
      }.andDo {
        handle { result ->
          val actualSleepLog = objectMapper.readValue<SleepLog>(result.response.contentAsByteArray)
          assertThat(actualSleepLog).isEqualTo(expectedSleepLog)
        }
      }
  }

  @Test
  fun `findLatest should return 404 when no sleep logs exist`() {
    // Given
    val userId = UUID.randomUUID()
    every { sleepLogService.findLatest(userId) } returns null

    // When/Then
    mockMvc.get("/api/v1/users/{user-id}/sleep-logs/latest", userId)
      .andExpect {
        status { isNotFound() }
      }
  }

  @Test
  fun `findById should return 200 with sleep log when exists`() {
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
    every { sleepLogService.findById(userId, sleepLogId) } returns expectedSleepLog

    // When/Then
    mockMvc.get("/api/v1/users/{user-id}/sleep-logs/{sleep-log-id}", userId, sleepLogId)
      .andExpect {
        status { isOk() }
      }.andDo {
        handle { result ->
          val actualSleepLog = objectMapper.readValue<SleepLog>(result.response.contentAsByteArray)
          assertThat(actualSleepLog).isEqualTo(expectedSleepLog)
        }
      }
  }

  @Test
  fun `findById should return 404 when sleep log does not exist`() {
    // Given
    val userId = UUID.randomUUID()
    val sleepLogId = UUID.randomUUID()
    every { sleepLogService.findById(userId, sleepLogId) } returns null

    // When/Then
    mockMvc.get("/api/v1/users/{user-id}/sleep-logs/{sleep-log-id}", userId, sleepLogId)
      .andExpect {
        status { isNotFound() }
      }
  }

}

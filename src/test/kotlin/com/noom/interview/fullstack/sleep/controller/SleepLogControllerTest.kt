package com.noom.interview.fullstack.sleep.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ninjasquad.springmockk.MockkBean
import com.noom.interview.fullstack.sleep.createSleepLog
import com.noom.interview.fullstack.sleep.createSleepLogRequest
import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.service.SleepLogService
import com.noom.interview.fullstack.sleep.toSleepLog
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Instant
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
    val request = createSleepLogRequest()
    val expectedSleepLog = request.toSleepLog(userId = userId)
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
    val request = createSleepLogRequest(
      bedTime = Instant.now().plus(8, ChronoUnit.HOURS) // bedTime is in a future
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
    val request = createSleepLogRequest(
      bedTime = Instant.now(),
      wakeTime = Instant.now().minus(8, ChronoUnit.HOURS) // wakeTime is before bedTime
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
      createSleepLog(userId = userId),
      createSleepLog(userId = userId)
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
    val expectedSleepLog = createSleepLog(userId = userId)
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
    val expectedSleepLog = createSleepLog(id = sleepLogId, userId = userId)
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

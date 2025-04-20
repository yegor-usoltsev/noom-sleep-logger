package com.noom.interview.fullstack.sleep.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ninjasquad.springmockk.MockkBean
import com.noom.interview.fullstack.sleep.*
import com.noom.interview.fullstack.sleep.model.Pagination
import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.model.SleepStats
import com.noom.interview.fullstack.sleep.service.SleepLogService
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.*
import java.time.LocalDate
import java.time.ZonedDateTime
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
      bedTime = ZonedDateTime.now(UTC).plus(8, ChronoUnit.HOURS) // bedTime is in a future
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
      bedTime = ZonedDateTime.now(UTC),
      wakeTime = ZonedDateTime.now(UTC).minus(8, ChronoUnit.HOURS) // wakeTime is before bedTime
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
    ).toPage()
    every { sleepLogService.findAll(userId, Pagination.fromPageAndSize(1, 2)) } returns expectedSleepLogs

    // When/Then
    mockMvc.get("/api/v1/users/{user-id}/sleep-logs", userId) {
      queryParam("page", "1")
      queryParam("page-size", "2")
    }.andExpect {
      status { isOk() }
      header { string(X_TOTAL_COUNT, "2") }
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

  @Test
  fun `updateById should return 200 with updated sleep log when exists`() {
    // Given
    val userId = UUID.randomUUID()
    val sleepLogId = UUID.randomUUID()
    val request = createSleepLogRequest()
    val expectedSleepLog = request.toSleepLog(id = sleepLogId, userId = userId)
    every { sleepLogService.updateById(userId, sleepLogId, request) } returns expectedSleepLog

    // When/Then
    mockMvc.put("/api/v1/users/{user-id}/sleep-logs/{sleep-log-id}", userId, sleepLogId) {
      contentType = MediaType.APPLICATION_JSON
      content = objectMapper.writeValueAsBytes(request)
    }.andExpect {
      status { isOk() }
    }.andDo {
      handle { result ->
        val actualSleepLog = objectMapper.readValue<SleepLog>(result.response.contentAsByteArray)
        assertThat(actualSleepLog).isEqualTo(expectedSleepLog)
      }
    }
  }

  @Test
  fun `updateById should return 404 when sleep log does not exist`() {
    // Given
    val userId = UUID.randomUUID()
    val sleepLogId = UUID.randomUUID()
    val request = createSleepLogRequest()
    every { sleepLogService.updateById(userId, sleepLogId, request) } returns null

    // When/Then
    mockMvc.put("/api/v1/users/{user-id}/sleep-logs/{sleep-log-id}", userId, sleepLogId) {
      contentType = MediaType.APPLICATION_JSON
      content = objectMapper.writeValueAsBytes(request)
    }.andExpect {
      status { isNotFound() }
    }
  }

  @Test
  fun `updateById should return 422 when wakeTime is before bedTime`() {
    // Given
    val userId = UUID.randomUUID()
    val sleepLogId = UUID.randomUUID()
    val request = createSleepLogRequest(
      bedTime = ZonedDateTime.now(UTC),
      wakeTime = ZonedDateTime.now(UTC).minus(8, ChronoUnit.HOURS) // wakeTime is before bedTime
    )

    // When/Then
    mockMvc.put("/api/v1/users/{user-id}/sleep-logs/{sleep-log-id}", userId, sleepLogId) {
      contentType = MediaType.APPLICATION_JSON
      content = objectMapper.writeValueAsBytes(request)
    }.andExpect {
      status { isUnprocessableEntity() }
    }
  }

  @Test
  fun `deleteById should return 204 when sleep log exists`() {
    // Given
    val userId = UUID.randomUUID()
    val sleepLogId = UUID.randomUUID()
    every { sleepLogService.deleteById(userId, sleepLogId) } returns true

    // When/Then
    mockMvc.delete("/api/v1/users/{user-id}/sleep-logs/{sleep-log-id}", userId, sleepLogId)
      .andExpect {
        status { isNoContent() }
      }
  }

  @Test
  fun `deleteById should return 404 when sleep log does not exist`() {
    // Given
    val userId = UUID.randomUUID()
    val sleepLogId = UUID.randomUUID()
    every { sleepLogService.deleteById(userId, sleepLogId) } returns false

    // When/Then
    mockMvc.delete("/api/v1/users/{user-id}/sleep-logs/{sleep-log-id}", userId, sleepLogId)
      .andExpect {
        status { isNotFound() }
      }
  }

  @Test
  fun `calculateSleepStats should return 200 with sleep stats when exists`() {
    // Given
    val userId = UUID.randomUUID()
    val daysBack = 30
    val expectedStats = createSleepStats(
      userId = userId,
      fromDate = LocalDate.now(UTC).minusDays(daysBack.toLong())
    )
    every { sleepLogService.calculateSleepStats(userId, daysBack) } returns expectedStats

    // When/Then
    mockMvc.get("/api/v1/users/{user-id}/sleep-logs/stats", userId) {
      queryParam("days-back", "$daysBack")
    }.andExpect {
      status { isOk() }
    }.andDo {
      handle { result ->
        val actualStats = objectMapper.readValue<SleepStats>(result.response.contentAsByteArray)
        assertThat(actualStats).isEqualTo(expectedStats)
      }
    }
  }

  @Test
  fun `calculateSleepStats should return 404 when no sleep logs exist`() {
    // Given
    val userId = UUID.randomUUID()
    every { sleepLogService.calculateSleepStats(userId, any()) } returns null

    // When/Then
    mockMvc.get("/api/v1/users/{user-id}/sleep-logs/stats", userId)
      .andExpect {
        status { isNotFound() }
      }
  }

}

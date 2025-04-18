package com.noom.interview.fullstack.sleep.controller

import com.noom.interview.fullstack.sleep.UnprocessableEntityException
import com.noom.interview.fullstack.sleep.model.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.service.SleepLogService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/users/{user-id}/sleep-logs")
class SleepLogController(private val sleepLogService: SleepLogService) {

  @PostMapping
  fun create(
    @PathVariable(value = "user-id") userId: UUID,
    @Valid @RequestBody newSleepLog: CreateSleepLogRequest
  ): ResponseEntity<SleepLog> {
    if (newSleepLog.bedTime >= newSleepLog.wakeTime) throw UnprocessableEntityException()
    val sleepLog = sleepLogService.create(userId, newSleepLog)
    return ResponseEntity(sleepLog, HttpStatus.CREATED)
  }

}

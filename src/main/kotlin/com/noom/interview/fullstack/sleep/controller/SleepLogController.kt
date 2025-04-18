package com.noom.interview.fullstack.sleep.controller

import com.noom.interview.fullstack.sleep.NotFoundException
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

  @GetMapping
  fun findAll(@PathVariable(value = "user-id") userId: UUID): ResponseEntity<List<SleepLog>> {
    val sleepLogs = sleepLogService.findAll(userId)
    return ResponseEntity(sleepLogs, HttpStatus.OK)
  }

  @GetMapping("/latest")
  fun findLatest(@PathVariable(value = "user-id") userId: UUID): ResponseEntity<SleepLog> {
    val sleepLog = sleepLogService.findLatest(userId) ?: throw NotFoundException()
    return ResponseEntity(sleepLog, HttpStatus.OK)
  }

  @GetMapping("/{sleep-log-id}")
  fun findById(
    @PathVariable(value = "user-id") userId: UUID,
    @PathVariable(value = "sleep-log-id") id: UUID
  ): ResponseEntity<SleepLog> {
    val sleepLog = sleepLogService.findById(userId, id) ?: throw NotFoundException()
    return ResponseEntity(sleepLog, HttpStatus.OK)
  }

  @PutMapping("/{sleep-log-id}")
  fun updateById(
    @PathVariable(value = "user-id") userId: UUID,
    @PathVariable(value = "sleep-log-id") id: UUID,
    @Valid @RequestBody newSleepLog: CreateSleepLogRequest
  ): ResponseEntity<SleepLog> {
    if (newSleepLog.bedTime >= newSleepLog.wakeTime) throw UnprocessableEntityException()
    val sleepLog = sleepLogService.updateById(userId, id, newSleepLog) ?: throw NotFoundException()
    return ResponseEntity(sleepLog, HttpStatus.OK)
  }

  @DeleteMapping("/{sleep-log-id}")
  fun deleteById(
    @PathVariable(value = "user-id") userId: UUID,
    @PathVariable(value = "sleep-log-id") id: UUID
  ): ResponseEntity<SleepLog> {
    val sleepLog = sleepLogService.deleteById(userId, id) ?: throw NotFoundException()
    return ResponseEntity(sleepLog, HttpStatus.OK)
  }

}

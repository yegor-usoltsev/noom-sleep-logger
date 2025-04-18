package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.model.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.repository.SleepLogRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class SleepLogService(private val sleepLogRepository: SleepLogRepository) {

  @Transactional
  fun create(userId: UUID, newSleepLog: CreateSleepLogRequest): SleepLog {
    return sleepLogRepository.create(userId, newSleepLog)
  }

  fun findAll(userId: UUID): List<SleepLog> {
    return sleepLogRepository.findAll(userId)
  }

  fun findLatest(userId: UUID): SleepLog? {
    return sleepLogRepository.findLatest(userId)
  }

  fun findById(userId: UUID, id: UUID): SleepLog? {
    return sleepLogRepository.findById(userId, id)
  }

  @Transactional
  fun updateById(userId: UUID, id: UUID, newSleepLog: CreateSleepLogRequest): SleepLog? {
    return sleepLogRepository.updateById(userId, id, newSleepLog)
  }

  @Transactional
  fun deleteById(userId: UUID, id: UUID): SleepLog? {
    return sleepLogRepository.deleteById(userId, id)
  }

}

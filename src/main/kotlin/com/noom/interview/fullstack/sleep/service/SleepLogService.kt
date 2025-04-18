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

}

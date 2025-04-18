package com.noom.interview.fullstack.sleep.repository

import com.noom.interview.fullstack.sleep.jooq.tables.SleepLogs.Companion.SLEEP_LOGS
import com.noom.interview.fullstack.sleep.jooq.tables.records.SleepLogsRecord
import com.noom.interview.fullstack.sleep.model.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.model.SleepLog
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
@Transactional
class SleepLogRepository(private val jooq: DSLContext) {

  fun create(userId: UUID, newSleepLog: CreateSleepLogRequest): SleepLog {
    return jooq
      .insertInto(SLEEP_LOGS)
      .set(
        SleepLogsRecord(
          userId = userId,
          bedTime = newSleepLog.bedTime,
          wakeTime = newSleepLog.wakeTime,
          mood = newSleepLog.mood
        )
      )
      .returning()
      .fetchSingle { it.toModel() }
  }

}

fun SleepLogsRecord.toModel(): SleepLog {
  return SleepLog(
    id = id!!,
    userId = userId,
    bedTime = bedTime,
    wakeTime = wakeTime,
    mood = mood,
    date = date!!,
    duration = duration!!.toDuration(),
    createdAt = createdAt!!,
    updatedAt = updatedAt!!
  )
}

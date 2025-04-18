package com.noom.interview.fullstack.sleep.repository

import com.noom.interview.fullstack.sleep.jooq.tables.SleepLogs.Companion.SLEEP_LOGS
import com.noom.interview.fullstack.sleep.jooq.tables.records.SleepLogsRecord
import com.noom.interview.fullstack.sleep.model.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.model.SleepLog
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
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

  fun findAll(userId: UUID): List<SleepLog> {
    return jooq
      .selectFrom(SLEEP_LOGS)
      .where(SLEEP_LOGS.USER_ID.eq(userId))
      .orderBy(SLEEP_LOGS.DATE.desc())
      .fetch { it.toModel() }
  }

  fun findLatest(userId: UUID): SleepLog? {
    return jooq
      .selectFrom(SLEEP_LOGS)
      .where(SLEEP_LOGS.USER_ID.eq(userId))
      .orderBy(SLEEP_LOGS.DATE.desc())
      .limit(1)
      .fetchOne { it.toModel() }
  }

  fun findById(userId: UUID, id: UUID): SleepLog? {
    return jooq
      .selectFrom(SLEEP_LOGS)
      .where(SLEEP_LOGS.USER_ID.eq(userId).and(SLEEP_LOGS.ID.eq(id)))
      .fetchOne { it.toModel() }
  }

  fun updateById(userId: UUID, id: UUID, newSleepLog: CreateSleepLogRequest): SleepLog? {
    return jooq
      .update(SLEEP_LOGS)
      .set(
        SleepLogsRecord(
          userId = userId,
          bedTime = newSleepLog.bedTime,
          wakeTime = newSleepLog.wakeTime,
          mood = newSleepLog.mood,
          updatedAt = Instant.now()
        )
      )
      .where(SLEEP_LOGS.USER_ID.eq(userId).and(SLEEP_LOGS.ID.eq(id)))
      .returning()
      .fetchOne { it.toModel() }
  }

  fun deleteById(userId: UUID, id: UUID): SleepLog? {
    return jooq
      .delete(SLEEP_LOGS)
      .where(SLEEP_LOGS.USER_ID.eq(userId).and(SLEEP_LOGS.ID.eq(id)))
      .returning()
      .fetchOne { it.toModel() }
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

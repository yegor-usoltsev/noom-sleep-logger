package com.noom.interview.fullstack.sleep.repository

import com.noom.interview.fullstack.sleep.jooq.enums.Mood
import com.noom.interview.fullstack.sleep.jooq.tables.SleepLogs.Companion.SLEEP_LOGS
import com.noom.interview.fullstack.sleep.jooq.tables.records.SleepLogsRecord
import com.noom.interview.fullstack.sleep.model.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.model.MoodFrequencies
import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.model.SleepStats
import org.jooq.DSLContext
import org.jooq.impl.DSL.*
import org.jooq.impl.SQLDataType.*
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
      .where(SLEEP_LOGS.USER_ID.eq(userId))
      .and(SLEEP_LOGS.ID.eq(id))
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
      .where(SLEEP_LOGS.USER_ID.eq(userId))
      .and(SLEEP_LOGS.ID.eq(id))
      .returning()
      .fetchOne { it.toModel() }
  }

  fun deleteById(userId: UUID, id: UUID): SleepLog? {
    return jooq
      .delete(SLEEP_LOGS)
      .where(SLEEP_LOGS.USER_ID.eq(userId))
      .and(SLEEP_LOGS.ID.eq(id))
      .returning()
      .fetchOne { it.toModel() }
  }

  fun calculateSleepStats(userId: UUID, daysBack: Int): SleepStats? {
    val toDate = currentLocalDate()
    val fromDate = toDate.minus(daysBack)

    val fromDateField = fromDate.`as`("from_date")
    val toDateField = toDate.`as`("to_date")
    val averageBedTimeField = avg(timestampDiff(SLEEP_LOGS.BED_TIME.cast(TIMESTAMP), SLEEP_LOGS.DATE.cast(TIMESTAMP)))
      .cast(LOCALTIME).`as`("average_bed_time")
    val averageWakeTimeField = avg(timestampDiff(SLEEP_LOGS.WAKE_TIME.cast(TIMESTAMP), SLEEP_LOGS.DATE.cast(TIMESTAMP)))
      .cast(LOCALTIME).`as`("average_wake_time")
    val averageDurationField = avg(SLEEP_LOGS.DURATION).cast(INTERVAL).`as`("average_duration")
    val moodBadField = count().filterWhere(SLEEP_LOGS.MOOD.eq(inline(Mood.BAD))).`as`("mood_bad")
    val moodOkField = count().filterWhere(SLEEP_LOGS.MOOD.eq(inline(Mood.OK))).`as`("mood_ok")
    val moodGoodField = count().filterWhere(SLEEP_LOGS.MOOD.eq(inline(Mood.GOOD))).`as`("mood_good")

    return jooq
      .select(
        SLEEP_LOGS.USER_ID,
        fromDateField,
        toDateField,
        averageBedTimeField,
        averageWakeTimeField,
        averageDurationField,
        moodBadField,
        moodOkField,
        moodGoodField
      )
      .from(SLEEP_LOGS)
      .where(SLEEP_LOGS.USER_ID.eq(userId))
      .and(SLEEP_LOGS.DATE.ge(fromDate))
      .groupBy(SLEEP_LOGS.USER_ID)
      .fetchOne {
        SleepStats(
          userId = it[SLEEP_LOGS.USER_ID]!!,
          fromDate = it[fromDateField],
          toDate = it[toDateField],
          averageBedTime = it[averageBedTimeField],
          averageWakeTime = it[averageWakeTimeField],
          averageDuration = it[averageDurationField].toDuration(),
          moodFrequencies = MoodFrequencies(
            bad = it[moodBadField],
            ok = it[moodOkField],
            good = it[moodGoodField]
          )
        )
      }
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

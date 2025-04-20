package com.noom.interview.fullstack.sleep.repository

import com.noom.interview.fullstack.sleep.applyPagination
import com.noom.interview.fullstack.sleep.jooq.enums.Mood
import com.noom.interview.fullstack.sleep.jooq.tables.SleepLogs.Companion.SLEEP_LOGS
import com.noom.interview.fullstack.sleep.jooq.tables.SleepLogsView.Companion.SLEEP_LOGS_VIEW
import com.noom.interview.fullstack.sleep.jooq.tables.records.SleepLogsRecord
import com.noom.interview.fullstack.sleep.jooq.tables.records.SleepLogsViewRecord
import com.noom.interview.fullstack.sleep.model.*
import org.jooq.DSLContext
import org.jooq.impl.DSL.*
import org.jooq.impl.SQLDataType.*
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.ZoneId
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
          bedTime = newSleepLog.bedTime.toInstant(),
          wakeTime = newSleepLog.wakeTime.toInstant(),
          mood = newSleepLog.mood
        )
      )
      .returning(SLEEP_LOGS.ID)
      .fetchSingle(SLEEP_LOGS.ID)!!
      .let { findById(userId, it)!! }
  }

  fun findAll(userId: UUID, pagination: Pagination? = null): List<SleepLog> {
    return jooq
      .selectFrom(SLEEP_LOGS_VIEW)
      .where(SLEEP_LOGS_VIEW.USER_ID.eq(userId))
      .orderBy(SLEEP_LOGS_VIEW.WAKE_TIME.desc())
      .applyPagination(pagination)
      .fetch { it.toModel() }
  }

  fun findLatest(userId: UUID): SleepLog? {
    return jooq
      .selectFrom(SLEEP_LOGS_VIEW)
      .where(SLEEP_LOGS_VIEW.USER_ID.eq(userId))
      .orderBy(SLEEP_LOGS_VIEW.WAKE_TIME.desc())
      .limit(1)
      .fetchOne { it.toModel() }
  }

  fun findById(userId: UUID, id: UUID): SleepLog? {
    return jooq
      .selectFrom(SLEEP_LOGS_VIEW)
      .where(SLEEP_LOGS_VIEW.USER_ID.eq(userId))
      .and(SLEEP_LOGS_VIEW.ID.eq(id))
      .fetchOne { it.toModel() }
  }

  fun updateById(userId: UUID, id: UUID, newSleepLog: CreateSleepLogRequest): SleepLog? {
    return jooq
      .update(SLEEP_LOGS)
      .set(
        SleepLogsRecord(
          userId = userId,
          bedTime = newSleepLog.bedTime.toInstant(),
          wakeTime = newSleepLog.wakeTime.toInstant(),
          mood = newSleepLog.mood,
          updatedAt = Instant.now()
        )
      )
      .where(SLEEP_LOGS.USER_ID.eq(userId))
      .and(SLEEP_LOGS.ID.eq(id))
      .returning(SLEEP_LOGS.ID)
      .fetchOne(SLEEP_LOGS.ID)
      ?.let { findById(userId, it)!! }
  }

  fun deleteById(userId: UUID, id: UUID): Boolean {
    return jooq
      .delete(SLEEP_LOGS)
      .where(SLEEP_LOGS.USER_ID.eq(userId))
      .and(SLEEP_LOGS.ID.eq(id))
      .execute() > 0
  }

  // TODO
  fun calculateSleepStats(userId: UUID, daysBack: Int): SleepStats? {
    val toDate = currentLocalDate()
    val fromDate = toDate.minus(daysBack)

    val fromDateField = fromDate.`as`("from_date")
    val toDateField = toDate.`as`("to_date")
    val averageBedTimeField = avg(
      timestampDiff(
        SLEEP_LOGS_VIEW.BED_TIME.cast(TIMESTAMP),
        SLEEP_LOGS_VIEW.WAKE_TIME.cast(DATE).cast(TIMESTAMP)
      )
    ).cast(LOCALTIME).`as`("average_bed_time")
    val averageWakeTimeField = avg(
      timestampDiff(
        SLEEP_LOGS_VIEW.WAKE_TIME.cast(TIMESTAMP),
        SLEEP_LOGS_VIEW.WAKE_TIME.cast(DATE).cast(TIMESTAMP)
      )
    ).cast(LOCALTIME).`as`("average_wake_time")
    val averageDurationField = avg(SLEEP_LOGS_VIEW.DURATION).cast(INTERVAL).`as`("average_duration")
    val moodBadField = count().filterWhere(SLEEP_LOGS_VIEW.MOOD.eq(inline(Mood.BAD))).`as`("mood_bad")
    val moodOkField = count().filterWhere(SLEEP_LOGS_VIEW.MOOD.eq(inline(Mood.OK))).`as`("mood_ok")
    val moodGoodField = count().filterWhere(SLEEP_LOGS_VIEW.MOOD.eq(inline(Mood.GOOD))).`as`("mood_good")

    return jooq
      .select(
        SLEEP_LOGS_VIEW.USER_ID,
        SLEEP_LOGS_VIEW.TIME_ZONE,
        fromDateField,
        toDateField,
        averageBedTimeField,
        averageWakeTimeField,
        averageDurationField,
        moodBadField,
        moodOkField,
        moodGoodField
      )
      .from(SLEEP_LOGS_VIEW)
      .where(SLEEP_LOGS_VIEW.USER_ID.eq(userId))
      .and(SLEEP_LOGS_VIEW.DATE.ge(fromDate))
      .groupBy(SLEEP_LOGS_VIEW.USER_ID, SLEEP_LOGS_VIEW.TIME_ZONE)
      .fetchOne {
        SleepStats(
          userId = it[SLEEP_LOGS_VIEW.USER_ID]!!,
          timeZone = ZoneId.of(it[SLEEP_LOGS_VIEW.TIME_ZONE]),
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

fun SleepLogsViewRecord.toModel(): SleepLog {
  val timeZone = ZoneId.of(timeZone)
  return SleepLog(
    id = id!!,
    userId = userId!!,
    timeZone = timeZone,
    bedTime = bedTime!!.atZone(timeZone),
    wakeTime = wakeTime!!.atZone(timeZone),
    mood = mood!!,
    date = date!!,
    duration = duration!!.toDuration(),
    createdAt = createdAt!!.atZone(timeZone),
    updatedAt = updatedAt!!.atZone(timeZone)
  )
}

package com.noom.interview.fullstack.sleep

import com.noom.interview.fullstack.sleep.jooq.enums.Mood
import com.noom.interview.fullstack.sleep.model.*
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.*

val LAX: ZoneId = ZoneId.of("America/Los_Angeles") // UTC-08:00 / UTC-07:00 (DST)
val WAW: ZoneId = ZoneId.of("Europe/Warsaw")       // UTC+01:00 / UTC+02:00 (DST)

fun createUser(
  id: UUID = UUID.randomUUID(),
  name: String = "${UUID.randomUUID()}",
  timeZone: ZoneId = UTC,
  createdAt: ZonedDateTime = ZonedDateTime.now(timeZone),
  updatedAt: ZonedDateTime = ZonedDateTime.now(timeZone)
): User = User(
  id = id,
  name = name,
  timeZone = timeZone,
  createdAt = createdAt,
  updatedAt = updatedAt
)

fun createUserRequest(
  name: String = "${UUID.randomUUID()}",
  timeZone: ZoneId = UTC
): CreateUserRequest = CreateUserRequest(
  name = name,
  timeZone = timeZone
)

fun CreateUserRequest.toUser(
  id: UUID = UUID.randomUUID(),
  createdAt: ZonedDateTime = ZonedDateTime.now(timeZone),
  updatedAt: ZonedDateTime = ZonedDateTime.now(timeZone)
): User = createUser(
  id = id,
  name = name,
  timeZone = timeZone,
  createdAt = createdAt,
  updatedAt = updatedAt
)

fun createSleepLog(
  id: UUID = UUID.randomUUID(),
  userId: UUID = UUID.randomUUID(),
  timeZone: ZoneId = UTC,
  bedTime: ZonedDateTime = ZonedDateTime.now(timeZone).minus(8, ChronoUnit.HOURS),
  wakeTime: ZonedDateTime = ZonedDateTime.now(timeZone),
  mood: Mood = Mood.entries.random(),
  date: LocalDate = wakeTime.toLocalDate(),
  duration: Duration = Duration.between(bedTime, wakeTime),
  createdAt: ZonedDateTime = ZonedDateTime.now(timeZone),
  updatedAt: ZonedDateTime = ZonedDateTime.now(timeZone)
): SleepLog = SleepLog(
  id = id,
  userId = userId,
  timeZone = timeZone,
  bedTime = bedTime,
  wakeTime = wakeTime,
  mood = mood,
  date = date,
  duration = duration,
  createdAt = createdAt,
  updatedAt = updatedAt
)

fun createSleepLogRequest(
  bedTime: ZonedDateTime = ZonedDateTime.now().minus(8, ChronoUnit.HOURS),
  wakeTime: ZonedDateTime = ZonedDateTime.now(),
  mood: Mood = Mood.entries.random()
): CreateSleepLogRequest = CreateSleepLogRequest(
  bedTime = bedTime,
  wakeTime = wakeTime,
  mood = mood
)

fun CreateSleepLogRequest.toSleepLog(
  id: UUID = UUID.randomUUID(),
  userId: UUID = UUID.randomUUID(),
  timeZone: ZoneId = UTC,
  date: LocalDate = wakeTime.toLocalDate(),
  duration: Duration = Duration.between(bedTime, wakeTime),
  createdAt: ZonedDateTime = ZonedDateTime.now(timeZone),
  updatedAt: ZonedDateTime = ZonedDateTime.now(timeZone)
): SleepLog = createSleepLog(
  id = id,
  userId = userId,
  timeZone = timeZone,
  bedTime = bedTime,
  wakeTime = wakeTime,
  mood = mood,
  date = date,
  duration = duration,
  createdAt = createdAt,
  updatedAt = updatedAt
)

fun createSleepStats(
  userId: UUID = UUID.randomUUID(),
  timeZone: ZoneId = UTC,
  fromDate: LocalDate = LocalDate.now(timeZone).minusDays(30),
  toDate: LocalDate = LocalDate.now(timeZone),
  averageBedTime: LocalTime = LocalTime.of(0, 30),
  averageWakeTime: LocalTime = LocalTime.of(8, 30),
  averageDuration: Duration = Duration.ofHours(8),
  moodFrequencies: MoodFrequencies = MoodFrequencies(bad = 1, ok = 2, good = 3)
): SleepStats = SleepStats(
  userId = userId,
  timeZone = timeZone,
  fromDate = fromDate,
  toDate = toDate,
  averageBedTime = averageBedTime,
  averageWakeTime = averageWakeTime,
  averageDuration = averageDuration,
  moodFrequencies = moodFrequencies
)

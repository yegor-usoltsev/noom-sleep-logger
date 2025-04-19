package com.noom.interview.fullstack.sleep

import com.noom.interview.fullstack.sleep.jooq.enums.Mood
import com.noom.interview.fullstack.sleep.model.*
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.*

fun createUser(
  id: UUID = UUID.randomUUID(),
  name: String = "${UUID.randomUUID()}",
  createdAt: Instant = Instant.now(),
  updatedAt: Instant = Instant.now()
): User = User(
  id = id,
  name = name,
  createdAt = createdAt,
  updatedAt = updatedAt
)

fun createUserRequest(
  name: String = "${UUID.randomUUID()}"
): CreateUserRequest = CreateUserRequest(
  name = name
)

fun CreateUserRequest.toUser(
  id: UUID = UUID.randomUUID(),
  createdAt: Instant = Instant.now(),
  updatedAt: Instant = Instant.now()
): User = createUser(
  id = id,
  name = name,
  createdAt = createdAt,
  updatedAt = updatedAt
)

fun createSleepLog(
  id: UUID = UUID.randomUUID(),
  userId: UUID = UUID.randomUUID(),
  bedTime: Instant = Instant.now().minus(8, ChronoUnit.HOURS),
  wakeTime: Instant = Instant.now(),
  mood: Mood = Mood.entries.random(),
  date: LocalDate = wakeTime.atOffset(ZoneOffset.UTC).toLocalDate(),
  duration: Duration = Duration.between(bedTime, wakeTime),
  createdAt: Instant = Instant.now(),
  updatedAt: Instant = Instant.now()
): SleepLog = SleepLog(
  id = id,
  userId = userId,
  bedTime = bedTime,
  wakeTime = wakeTime,
  mood = mood,
  date = date,
  duration = duration,
  createdAt = createdAt,
  updatedAt = updatedAt
)

fun createSleepLogRequest(
  bedTime: Instant = Instant.now().minus(8, ChronoUnit.HOURS),
  wakeTime: Instant = Instant.now(),
  mood: Mood = Mood.entries.random()
): CreateSleepLogRequest = CreateSleepLogRequest(
  bedTime = bedTime,
  wakeTime = wakeTime,
  mood = mood
)

fun CreateSleepLogRequest.toSleepLog(
  id: UUID = UUID.randomUUID(),
  userId: UUID = UUID.randomUUID(),
  date: LocalDate = wakeTime.atOffset(ZoneOffset.UTC).toLocalDate(),
  duration: Duration = Duration.between(bedTime, wakeTime),
  createdAt: Instant = Instant.now(),
  updatedAt: Instant = Instant.now()
): SleepLog = createSleepLog(
  id = id,
  userId = userId,
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
  fromDate: LocalDate = LocalDate.now().minusDays(30),
  toDate: LocalDate = LocalDate.now(),
  averageBedTime: LocalTime = LocalTime.of(0, 30),
  averageWakeTime: LocalTime = LocalTime.of(8, 30),
  averageDuration: Duration = Duration.ofHours(8),
  moodFrequencies: MoodFrequencies = MoodFrequencies(bad = 1, ok = 2, good = 3)
): SleepStats = SleepStats(
  userId = userId,
  fromDate = fromDate,
  toDate = toDate,
  averageBedTime = averageBedTime,
  averageWakeTime = averageWakeTime,
  averageDuration = averageDuration,
  moodFrequencies = moodFrequencies
)

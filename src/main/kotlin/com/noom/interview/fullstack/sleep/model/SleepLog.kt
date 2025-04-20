package com.noom.interview.fullstack.sleep.model

import com.noom.interview.fullstack.sleep.jooq.enums.Mood
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.PastOrPresent
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

data class SleepLog(
  val id: UUID,
  val userId: UUID,
  val timeZone: ZoneId,
  val bedTime: ZonedDateTime,
  val wakeTime: ZonedDateTime,
  val mood: Mood,
  val date: LocalDate,
  val duration: Duration,
  val createdAt: ZonedDateTime,
  val updatedAt: ZonedDateTime
)

data class CreateSleepLogRequest(
  @field:Past
  val bedTime: ZonedDateTime,
  @field:PastOrPresent
  val wakeTime: ZonedDateTime,
  val mood: Mood
)

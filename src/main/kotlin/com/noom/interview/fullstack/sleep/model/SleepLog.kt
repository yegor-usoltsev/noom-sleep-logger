package com.noom.interview.fullstack.sleep.model

import com.noom.interview.fullstack.sleep.jooq.enums.Mood
import jakarta.validation.constraints.PastOrPresent
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.*

data class SleepLog(
  val id: UUID,
  val userId: UUID,
  val bedTime: Instant,
  val wakeTime: Instant,
  val mood: Mood,
  val date: LocalDate,
  val duration: Duration,
  val createdAt: Instant,
  val updatedAt: Instant
)

data class CreateSleepLogRequest(
  @field:PastOrPresent
  val bedTime: Instant,
  @field:PastOrPresent
  val wakeTime: Instant,
  val mood: Mood
)

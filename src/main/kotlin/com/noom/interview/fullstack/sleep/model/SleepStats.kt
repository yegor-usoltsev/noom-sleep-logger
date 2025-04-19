package com.noom.interview.fullstack.sleep.model

import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

data class SleepStats(
  val userId: UUID,
  val fromDate: LocalDate,
  val toDate: LocalDate,
  val averageBedTime: LocalTime,
  val averageWakeTime: LocalTime,
  val averageDuration: Duration,
  val moodFrequencies: MoodFrequencies
)

data class MoodFrequencies(
  val bad: Int,
  val ok: Int,
  val good: Int
)

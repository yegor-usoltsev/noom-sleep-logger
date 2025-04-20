package com.noom.interview.fullstack.sleep.model

import com.noom.interview.fullstack.sleep.UTC
import jakarta.validation.constraints.Pattern
import java.time.Instant
import java.time.ZoneId
import java.util.*

data class User(
  val id: UUID,
  val name: String,
  val timeZone: ZoneId,
  val createdAt: Instant,
  val updatedAt: Instant
)

data class CreateUserRequest(
  @field:Pattern(regexp = "^[a-zA-Z0-9_-]{1,50}$")
  val name: String,
  val timeZone: ZoneId = UTC
)

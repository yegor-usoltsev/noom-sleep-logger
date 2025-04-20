package com.noom.interview.fullstack.sleep.model

import jakarta.validation.constraints.Pattern
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

data class User(
  val id: UUID,
  val name: String,
  val timeZone: ZoneId,
  val createdAt: ZonedDateTime,
  val updatedAt: ZonedDateTime
)

data class CreateUserRequest(
  @field:Pattern(regexp = "^[a-zA-Z0-9_-]{1,50}$")
  val name: String,
  val timeZone: ZoneId = ZoneOffset.UTC
)

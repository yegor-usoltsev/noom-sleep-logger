package com.noom.interview.fullstack.sleep.model

import jakarta.validation.constraints.Pattern
import java.time.LocalDateTime
import java.util.*

data class User(
  val id: UUID,
  val name: String,
  val createdAt: LocalDateTime,
  val updatedAt: LocalDateTime
)

data class CreateUserRequest(
  @field:Pattern(regexp = "^[a-zA-Z0-9_-]{1,50}$")
  val name: String
)

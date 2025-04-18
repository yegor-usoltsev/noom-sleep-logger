package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.model.CreateUserRequest
import com.noom.interview.fullstack.sleep.model.User
import com.noom.interview.fullstack.sleep.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

class UserServiceTest {

  private val userRepository = mockk<UserRepository>()
  private val userService = UserService(userRepository)

  @Test
  fun create() {
    // Given
    val request = CreateUserRequest(name = "test-user")
    val expectedUser = User(
      id = UUID.randomUUID(),
      name = request.name,
      createdAt = Instant.now(),
      updatedAt = Instant.now()
    )
    every { userRepository.create(request) } returns expectedUser

    // When
    val result = userService.create(request)

    // Then
    assertThat(result).isEqualTo(expectedUser)
    verify(exactly = 1) { userRepository.create(request) }
  }

  @Test
  fun findAll() {
    // Given
    val expectedUsers = listOf(
      User(
        id = UUID.randomUUID(),
        name = "user1",
        createdAt = Instant.now(),
        updatedAt = Instant.now()
      ),
      User(
        id = UUID.randomUUID(),
        name = "user2",
        createdAt = Instant.now(),
        updatedAt = Instant.now()
      )
    )
    every { userRepository.findAll() } returns expectedUsers

    // When
    val result = userService.findAll()

    // Then
    assertThat(result).isEqualTo(expectedUsers)
    verify(exactly = 1) { userRepository.findAll() }
  }

  @Test
  fun findById() {
    // Given
    val userId = UUID.randomUUID()
    val expectedUser = User(
      id = userId,
      name = "test-user",
      createdAt = Instant.now(),
      updatedAt = Instant.now()
    )
    every { userRepository.findById(userId) } returns expectedUser

    // When
    val result = userService.findById(userId)

    // Then
    assertThat(result).isEqualTo(expectedUser)
    verify(exactly = 1) { userRepository.findById(userId) }
  }

}

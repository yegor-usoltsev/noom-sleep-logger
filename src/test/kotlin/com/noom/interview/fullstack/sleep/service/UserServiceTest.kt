package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.createUser
import com.noom.interview.fullstack.sleep.createUserRequest
import com.noom.interview.fullstack.sleep.repository.UserRepository
import com.noom.interview.fullstack.sleep.toUser
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class UserServiceTest {

  private val userRepository = mockk<UserRepository>()
  private val userService = UserService(userRepository)

  @Test
  fun create() {
    // Given
    val request = createUserRequest()
    val expectedUser = request.toUser()
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
      createUser(),
      createUser()
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
    val expectedUser = createUser(id = userId)
    every { userRepository.findById(userId) } returns expectedUser

    // When
    val result = userService.findById(userId)

    // Then
    assertThat(result).isEqualTo(expectedUser)
    verify(exactly = 1) { userRepository.findById(userId) }
  }

  @Test
  fun updateById() {
    // Given
    val userId = UUID.randomUUID()
    val request = createUserRequest()
    val expectedUser = request.toUser(id = userId)
    every { userRepository.updateById(userId, request) } returns expectedUser

    // When
    val result = userService.updateById(userId, request)

    // Then
    assertThat(result).isEqualTo(expectedUser)
    verify(exactly = 1) { userRepository.updateById(userId, request) }
  }

}

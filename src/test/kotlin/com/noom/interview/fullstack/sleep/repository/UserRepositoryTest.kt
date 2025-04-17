package com.noom.interview.fullstack.sleep.repository

import com.noom.interview.fullstack.sleep.IntegrationTest
import com.noom.interview.fullstack.sleep.model.CreateUserRequest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.ThrowableAssert.ThrowingCallable
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
import java.util.*

class UserRepositoryTest @Autowired constructor(private val userRepository: UserRepository) : IntegrationTest() {

  @Test
  fun `create should create a new user`() {
    // Given
    val newUser = CreateUserRequest(name = "test-user")
    // When
    val createdUser = userRepository.create(newUser)
    // Then
    assertThat(createdUser).isNotNull()
    assertThat(createdUser.id).isNotNull()
    assertThat(createdUser.name).isEqualTo("test-user")
    assertThat(createdUser.createdAt).isNotNull()
    assertThat(createdUser.updatedAt).isNotNull()
  }

  @Test
  fun `create should throw when name is not unique`() {
    // Given
    val newUser = CreateUserRequest(name = "test-user")
    userRepository.create(newUser)
    // When
    val code = ThrowingCallable { userRepository.create(newUser) }
    // Then
    assertThatThrownBy(code).isInstanceOf(DuplicateKeyException::class.java)
  }

  @Test
  fun `findAll should return all users`() {
    // Given
    val user1 = userRepository.create(CreateUserRequest(name = "user1"))
    val user2 = userRepository.create(CreateUserRequest(name = "user2"))
    // When
    val users = userRepository.findAll()
    // Then
    assertThat(users).hasSizeGreaterThanOrEqualTo(2)
    assertThat(users).contains(user1, user2)
  }

  @Test
  fun `findById should return user when exists`() {
    // Given
    val createdUser = userRepository.create(CreateUserRequest(name = "test-user"))
    // When
    val foundUser = userRepository.findById(createdUser.id)
    // Then
    assertThat(foundUser).isNotNull()
    assertThat(foundUser).isEqualTo(createdUser)
  }

  @Test
  fun `findById should return null when user does not exist`() {
    // Given
    val nonExistentId = UUID.randomUUID()
    // When
    val foundUser = userRepository.findById(nonExistentId)
    // Then
    assertThat(foundUser).isNull()
  }

}

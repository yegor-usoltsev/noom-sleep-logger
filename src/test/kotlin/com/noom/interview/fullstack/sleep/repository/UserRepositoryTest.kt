package com.noom.interview.fullstack.sleep.repository

import com.noom.interview.fullstack.sleep.createUserRequest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.ThrowableAssert.ThrowingCallable
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jooq.JooqTest
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import java.util.*

@JooqTest
@Import(UserRepository::class)
class UserRepositoryTest @Autowired constructor(private val userRepository: UserRepository) {

  @Test
  fun `create should create a new user`() {
    // Given
    val newUser = createUserRequest(name = " TEST-user ")

    // When
    val createdUser = userRepository.create(newUser)

    // Then
    assertThat(createdUser.id).isNotNull()
    assertThat(createdUser.name).isEqualTo(newUser.name.trim().lowercase())
    assertThat(createdUser.createdAt).isNotNull()
    assertThat(createdUser.updatedAt).isNotNull()
  }

  @Test
  fun `create should throw when name is not unique`() {
    // Given
    val newUser1 = createUserRequest(name = " TEST-user ")
    val newUser2 = createUserRequest(name = " test-USER ")
    userRepository.create(newUser1)

    // When
    val code = ThrowingCallable { userRepository.create(newUser2) }

    // Then
    assertThatThrownBy(code).isInstanceOf(DuplicateKeyException::class.java)
  }

  @Test
  fun `create should throw when name is not valid`() {
    // Given
    val newUser = createUserRequest(name = "") // name is not valid

    // When
    val code = ThrowingCallable { userRepository.create(newUser) }

    // Then
    assertThatThrownBy(code).isInstanceOf(DataIntegrityViolationException::class.java)
  }

  @Test
  fun `findAll should return all users`() {
    // Given
    val user1 = userRepository.create(createUserRequest())
    val user2 = userRepository.create(createUserRequest())

    // When
    val users = userRepository.findAll()

    // Then
    assertThat(users).hasSizeGreaterThanOrEqualTo(2)
    assertThat(users).contains(user1, user2)
  }

  @Test
  fun `findById should return user when exists`() {
    // Given
    val createdUser = userRepository.create(createUserRequest())

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

package com.noom.interview.fullstack.sleep.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ninjasquad.springmockk.MockkBean
import com.noom.interview.fullstack.sleep.createUser
import com.noom.interview.fullstack.sleep.createUserRequest
import com.noom.interview.fullstack.sleep.model.Pagination
import com.noom.interview.fullstack.sleep.model.User
import com.noom.interview.fullstack.sleep.service.UserService
import com.noom.interview.fullstack.sleep.toUser
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.util.*

@WebMvcTest(UserController::class)
class UserControllerTest @Autowired constructor(
  private val mockMvc: MockMvc,
  private val objectMapper: ObjectMapper
) {

  @MockkBean
  private lateinit var userService: UserService

  @Test
  fun `create should return 201 with created user`() {
    // Given
    val request = createUserRequest()
    val expectedUser = request.toUser()
    every { userService.create(request) } returns expectedUser

    // When/Then
    mockMvc.post("/api/v1/users") {
      contentType = MediaType.APPLICATION_JSON
      content = objectMapper.writeValueAsBytes(request)
    }.andExpect {
      status { isCreated() }
    }.andDo {
      handle { result ->
        val actualUser = objectMapper.readValue<User>(result.response.contentAsByteArray)
        assertThat(actualUser).isEqualTo(expectedUser)
      }
    }
  }

  @Test
  fun `create should return 400 when name is not valid`() {
    // Given
    val request = createUserRequest(name = "") // name is not valid

    // When/Then
    mockMvc.post("/api/v1/users") {
      contentType = MediaType.APPLICATION_JSON
      content = objectMapper.writeValueAsBytes(request)
    }.andExpect {
      status { isBadRequest() }
    }
  }

  @Test
  fun `findAll should return 200 with all users`() {
    // Given
    val expectedUsers = listOf(
      createUser(),
      createUser()
    )
    every { userService.findAll(Pagination.fromPageAndSize(1, 2)) } returns expectedUsers

    // When/Then
    mockMvc.get("/api/v1/users") {
      queryParam("page", "1")
      queryParam("pageSize", "2")
    }.andExpect {
      status { isOk() }
    }.andDo {
      handle { result ->
        val actualUsers = objectMapper.readValue<List<User>>(result.response.contentAsByteArray)
        assertThat(actualUsers).isEqualTo(expectedUsers)
      }
    }
  }

  @Test
  fun `findById should return 200 with user when exists`() {
    // Given
    val userId = UUID.randomUUID()
    val expectedUser = createUser(id = userId)
    every { userService.findById(userId) } returns expectedUser

    // When/Then
    mockMvc.get("/api/v1/users/{user-id}", userId)
      .andExpect {
        status { isOk() }
      }.andDo {
        handle { result ->
          val actualUsers = objectMapper.readValue<User>(result.response.contentAsByteArray)
          assertThat(actualUsers).isEqualTo(expectedUser)
        }
      }
  }

  @Test
  fun `findById should return 404 when user does not exist`() {
    // Given
    val userId = UUID.randomUUID()
    every { userService.findById(userId) } returns null

    // When/Then
    mockMvc.get("/api/v1/users/{user-id}", userId)
      .andExpect {
        status { isNotFound() }
      }
  }

}

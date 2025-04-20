package com.noom.interview.fullstack.sleep.controller

import com.noom.interview.fullstack.sleep.NotFoundException
import com.noom.interview.fullstack.sleep.model.CreateUserRequest
import com.noom.interview.fullstack.sleep.model.Pagination
import com.noom.interview.fullstack.sleep.model.User
import com.noom.interview.fullstack.sleep.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/users")
class UserController(private val userService: UserService) {

  @PostMapping
  fun create(@Valid @RequestBody newUser: CreateUserRequest): ResponseEntity<User> {
    val user = userService.create(newUser)
    return ResponseEntity(user, HttpStatus.CREATED)
  }

  @GetMapping
  fun findAll(
    @RequestParam(value = "page", required = false, defaultValue = "1") page: Int,
    @RequestParam(value = "page-size", required = false, defaultValue = "20") pageSize: Int
  ): ResponseEntity<List<User>> {
    val pagination = Pagination.fromPageAndSize(page, pageSize)
    val users = userService.findAll(pagination)
    return ResponseEntity(users, HttpStatus.OK)
  }

  @GetMapping("/{user-id}")
  fun findById(@PathVariable(value = "user-id") id: UUID): ResponseEntity<User> {
    val user = userService.findById(id) ?: throw NotFoundException()
    return ResponseEntity(user, HttpStatus.OK)
  }

  @PutMapping("/{user-id}")
  fun updateById(
    @PathVariable(value = "user-id") id: UUID,
    @Valid @RequestBody newUser: CreateUserRequest
  ): ResponseEntity<User> {
    val user = userService.updateById(id, newUser) ?: throw NotFoundException()
    return ResponseEntity(user, HttpStatus.OK)
  }

}

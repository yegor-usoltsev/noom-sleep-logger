package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.model.CreateUserRequest
import com.noom.interview.fullstack.sleep.model.User
import com.noom.interview.fullstack.sleep.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserService(private val userRepository: UserRepository) {

  @Transactional
  fun create(newUser: CreateUserRequest): User {
    return userRepository.create(newUser)
  }

  fun findAll(): List<User> {
    return userRepository.findAll()
  }

  fun findById(id: UUID): User? {
    return userRepository.findById(id)
  }

}

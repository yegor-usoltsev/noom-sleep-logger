package com.noom.interview.fullstack.sleep.repository

import com.noom.interview.fullstack.sleep.applyPagination
import com.noom.interview.fullstack.sleep.jooq.tables.Users.Companion.USERS
import com.noom.interview.fullstack.sleep.jooq.tables.records.UsersRecord
import com.noom.interview.fullstack.sleep.model.CreateUserRequest
import com.noom.interview.fullstack.sleep.model.Pagination
import com.noom.interview.fullstack.sleep.model.User
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
@Transactional
class UserRepository(private val jooq: DSLContext) {

  fun create(newUser: CreateUserRequest): User {
    return jooq
      .insertInto(USERS)
      .set(UsersRecord(name = newUser.name.trim().lowercase()))
      .returning()
      .fetchSingle { it.toModel() }
  }

  fun findAll(pagination: Pagination? = null): List<User> {
    return jooq
      .selectFrom(USERS)
      .orderBy(USERS.CREATED_AT.desc())
      .applyPagination(pagination)
      .fetch { it.toModel() }
  }

  fun findById(id: UUID): User? {
    return jooq
      .selectFrom(USERS)
      .where(USERS.ID.eq(id))
      .fetchOne { it.toModel() }
  }

}

fun UsersRecord.toModel(): User {
  return User(
    id = id!!,
    name = name,
    createdAt = createdAt!!,
    updatedAt = updatedAt!!
  )
}

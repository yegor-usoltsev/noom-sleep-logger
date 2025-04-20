package com.noom.interview.fullstack.sleep.repository

import com.noom.interview.fullstack.sleep.applyPagination
import com.noom.interview.fullstack.sleep.jooq.tables.Users.Companion.USERS
import com.noom.interview.fullstack.sleep.jooq.tables.records.UsersRecord
import com.noom.interview.fullstack.sleep.model.CreateUserRequest
import com.noom.interview.fullstack.sleep.model.Page
import com.noom.interview.fullstack.sleep.model.Pagination
import com.noom.interview.fullstack.sleep.model.User
import org.jooq.DSLContext
import org.jooq.kotlin.fetchSingleValue
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.ZoneId
import java.util.*

@Repository
@Transactional
class UserRepository(private val jooq: DSLContext) {

  fun create(newUser: CreateUserRequest): User {
    return jooq
      .insertInto(USERS)
      .set(
        UsersRecord(
          name = newUser.name.trim().lowercase(),
          timeZone = newUser.timeZone.id
        )
      )
      .returning()
      .fetchSingle { it.toModel() }
  }

  fun findAll(pagination: Pagination? = null): Page<User> {
    return Page(
      list = jooq
        .selectFrom(USERS)
        .orderBy(USERS.CREATED_AT.desc())
        .applyPagination(pagination)
        .fetch { it.toModel() },
      totalSize = jooq
        .selectCount()
        .from(USERS)
        .fetchSingleValue()
    )
  }

  fun findById(id: UUID): User? {
    return jooq
      .selectFrom(USERS)
      .where(USERS.ID.eq(id))
      .fetchOne { it.toModel() }
  }

  fun updateById(id: UUID, newUser: CreateUserRequest): User? {
    return jooq
      .update(USERS)
      .set(
        UsersRecord(
          name = newUser.name.trim().lowercase(),
          timeZone = newUser.timeZone.id,
          updatedAt = Instant.now()
        )
      )
      .where(USERS.ID.eq(id))
      .returning(USERS.ID)
      .fetchOne(USERS.ID)
      ?.let { findById(it)!! }
  }

}

fun UsersRecord.toModel(): User {
  return User(
    id = id!!,
    name = name,
    timeZone = ZoneId.of(timeZone),
    createdAt = createdAt!!,
    updatedAt = updatedAt!!
  )
}

package com.noom.interview.fullstack.sleep

import org.jooq.DSLContext
import org.jooq.impl.DSL.currentLocalDate
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.MediaType.ALL_VALUE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@SpringBootApplication
class App

fun main(args: Array<String>) {
  runApplication<App>(*args)
}

@RestController
@RequestMapping("/api/v1")
class PingController(private val jooq: DSLContext) {

  @GetMapping("/ping", consumes = [ALL_VALUE], produces = [APPLICATION_JSON_VALUE])
  fun ping(): ResponseEntity<Pong> {
    val dateField = currentLocalDate()
    val date = jooq.select(dateField)
      .fetchSingle()
      .map { it[dateField] }
    return ResponseEntity.ok(Pong(date))
  }

}

data class Pong(val date: LocalDate)

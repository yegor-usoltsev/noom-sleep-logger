package com.noom.interview.fullstack.sleep

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.LocalDate

class PingControllerTest @Autowired constructor(private val mockMvc: MockMvc) : IntegrationTest() {

  @Test
  fun ping() {
    mockMvc.get("/api/v1/ping").andExpect {
      status { isOk() }
      content { contentType(MediaType.APPLICATION_JSON) }
      content { json("""{"date":"${LocalDate.now()}"}""") }
    }
  }

}

package com.noom.interview.fullstack.sleep

import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus.*
import org.springframework.web.context.request.ServletWebRequest

class GlobalExceptionHandlerTest {

  private val handler = GlobalExceptionHandler()
  private val request = ServletWebRequest(mockk<HttpServletRequest>())

  @Test
  fun `should return 409 Conflict for DuplicateKeyException`() {
    val exception = DuplicateKeyException("Duplicate key")
    val response = handler.handleThrowable(exception, request)
    assertThat(response?.statusCode).isEqualTo(CONFLICT)
  }

  @Test
  fun `should return 400 Bad Request for DataIntegrityViolationException`() {
    val exception = DataIntegrityViolationException("Data integrity violation")
    val response = handler.handleThrowable(exception, request)
    assertThat(response?.statusCode).isEqualTo(BAD_REQUEST)
  }

  @Test
  fun `should return 500 Internal Server Error for generic exception`() {
    val exception = Exception("Generic error")
    val response = handler.handleThrowable(exception, request)
    assertThat(response?.statusCode).isEqualTo(INTERNAL_SERVER_ERROR)
  }

}

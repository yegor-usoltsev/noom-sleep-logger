package com.noom.interview.fullstack.sleep

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus.*
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

  @ExceptionHandler(Exception::class)
  fun handleThrowable(ex: Exception, request: WebRequest): ResponseEntity<Any>? {
    val statusCode = when (ex) {
      is DuplicateKeyException -> CONFLICT
      is DataIntegrityViolationException -> BAD_REQUEST
      is IllegalArgumentException -> BAD_REQUEST
      else -> INTERNAL_SERVER_ERROR
    }
    val body = ProblemDetail.forStatusAndDetail(statusCode, ex.message)
    return handleExceptionInternal(ex, body, HttpHeaders(), statusCode, request)
  }

}

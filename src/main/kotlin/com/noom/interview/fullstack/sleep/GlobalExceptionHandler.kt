package com.noom.interview.fullstack.sleep

import org.springframework.dao.*
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus.*
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.BadSqlGrammarException
import org.springframework.jdbc.CannotGetJdbcConnectionException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

  @ExceptionHandler(Exception::class)
  fun handleThrowable(ex: Exception, request: WebRequest): ResponseEntity<Any>? {
    val statusCode = when (ex) {
      is BadSqlGrammarException -> INTERNAL_SERVER_ERROR
      is CannotAcquireLockException -> CONFLICT
      is CannotGetJdbcConnectionException -> SERVICE_UNAVAILABLE
      is DataAccessResourceFailureException -> SERVICE_UNAVAILABLE
      is DuplicateKeyException -> CONFLICT
      is DataIntegrityViolationException -> BAD_REQUEST
      is EmptyResultDataAccessException -> NOT_FOUND
      is IncorrectResultSizeDataAccessException -> INTERNAL_SERVER_ERROR
      is InvalidDataAccessApiUsageException -> BAD_REQUEST
      is OptimisticLockingFailureException -> CONFLICT
      is PermissionDeniedDataAccessException -> FORBIDDEN
      is PessimisticLockingFailureException -> CONFLICT
      is QueryTimeoutException -> REQUEST_TIMEOUT
      else -> INTERNAL_SERVER_ERROR
    }
    val body = ProblemDetail.forStatusAndDetail(statusCode, ex.message)
    return handleExceptionInternal(ex, body, HttpHeaders(), statusCode, request)
  }

}

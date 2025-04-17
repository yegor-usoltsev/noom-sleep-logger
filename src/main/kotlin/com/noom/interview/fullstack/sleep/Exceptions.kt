package com.noom.interview.fullstack.sleep

import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatusCode
import org.springframework.web.ErrorResponseException

class NotFoundException(cause: Throwable? = null) : AppException(NOT_FOUND, cause)

open class AppException(status: HttpStatusCode, cause: Throwable? = null) : ErrorResponseException(status, cause) {
  init {
    cause?.let { setDetail(it.message) }
  }
}

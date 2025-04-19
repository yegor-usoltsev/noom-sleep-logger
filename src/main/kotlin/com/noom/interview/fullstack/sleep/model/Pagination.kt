package com.noom.interview.fullstack.sleep.model

data class Pagination(
  val limit: Int = 20,
  val offset: Int = 0
) {

  init {
    require(offset >= 0) { "Offset must be non-negative" }
    require(limit > 0) { "Limit must be positive" }
    require(limit <= 100) { "Limit must be less than or equal to 100" }
  }

  companion object {
    fun fromPageAndSize(page: Int = 1, pageSize: Int = 20): Pagination {
      require(page > 0) { "Page must be positive" }
      require(pageSize > 0) { "Page size must be positive" }
      require(pageSize <= 100) { "Page size must be less than or equal to 100" }
      return Pagination(
        limit = pageSize,
        offset = pageSize * (page - 1)
      )
    }
  }

}

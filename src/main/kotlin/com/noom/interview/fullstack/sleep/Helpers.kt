package com.noom.interview.fullstack.sleep

import com.noom.interview.fullstack.sleep.model.Pagination
import org.jooq.Record
import org.jooq.SelectForUpdateStep
import org.jooq.SelectLimitStep

fun <R : Record> SelectLimitStep<R>.applyPagination(pagination: Pagination?): SelectForUpdateStep<R> {
  return if (pagination != null) this.limit(pagination.limit).offset(pagination.offset) else this
}

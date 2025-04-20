package com.noom.interview.fullstack.sleep

import com.noom.interview.fullstack.sleep.model.Pagination
import org.jooq.Field
import org.jooq.Record
import org.jooq.SelectForUpdateStep
import org.jooq.SelectLimitStep
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType.TIMESTAMP
import java.sql.Timestamp
import java.time.ZoneId

val UTC: ZoneId = ZoneId.of("UTC")

fun <R : Record> SelectLimitStep<R>.applyPagination(pagination: Pagination?): SelectForUpdateStep<R> {
  return if (pagination != null) this.limit(pagination.limit).offset(pagination.offset) else this
}

fun Field<Timestamp?>.atTimeZone(timeZone: Field<String?>): Field<Timestamp?> {
  return DSL.field("{0} at time zone {1}", TIMESTAMP, this, timeZone)
}

package com.noom.interview.fullstack.sleep

import com.noom.interview.fullstack.sleep.model.Pagination
import org.jooq.Field
import org.jooq.Record
import org.jooq.SelectForUpdateStep
import org.jooq.SelectLimitStep
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType.TIMESTAMP
import org.springframework.util.MultiValueMap
import java.sql.Timestamp
import java.time.ZoneId

const val X_TOTAL_COUNT = "X-Total-Count"

val UTC: ZoneId = ZoneId.of("UTC")

fun <R : Record> SelectLimitStep<R>.applyPagination(pagination: Pagination?): SelectForUpdateStep<R> {
  return if (pagination != null) this.limit(pagination.limit).offset(pagination.offset) else this
}

fun Field<Timestamp?>.atTimeZone(timeZone: Field<String?>): Field<Timestamp?> {
  return DSL.field("{0} at time zone {1}", TIMESTAMP, this, timeZone)
}

fun headersOf(vararg pairs: Pair<String, String>): MultiValueMap<String, String> = MultiValueMap.fromSingleValue(
  mapOf(*pairs)
)

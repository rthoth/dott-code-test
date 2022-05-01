package dott.report

import java.time.temporal.ChronoUnit
import java.time.{OffsetDateTime, Year}

object RangeDescriptor {

  def beginOfMonth(dateTime: OffsetDateTime): OffsetDateTime = {
    dateTime
      .withDayOfMonth(1)
      .truncatedTo(ChronoUnit.DAYS)
  }

  def endOfMonth(dateTime: OffsetDateTime): OffsetDateTime = {
    val month = dateTime.getMonth
    dateTime
      .withDayOfMonth(if (!Year.isLeap(dateTime.getYear)) month.minLength() else month.maxLength())
      .withHour(23)
      .withMinute(59)
      .withSecond(59)
      .withNano(999999999)
  }

}

case class BetweenRangeDescriptor(start: Int, end: Int) extends RangeDescriptor {

  require(end > start, s"Invalid Between Group $start-$end!")

  override def reference(reference: OffsetDateTime): Range = {
    BetweenRange(
      RangeDescriptor.beginOfMonth(reference.minusMonths(end)),
      RangeDescriptor.endOfMonth(reference.minusMonths(start)),
      end,
      start
    )
  }
}

case class BeforeThanRangeDescriptor(quantity: Int) extends RangeDescriptor {

  require(quantity > 0, s"Invalid Greater Than Group $quantity!")

  override def reference(reference: OffsetDateTime): Range = {
    BeforeThanRange(RangeDescriptor.beginOfMonth(reference.minusMonths(quantity)), quantity)
  }
}

sealed trait RangeDescriptor {

  def reference(reference: OffsetDateTime): Range
}

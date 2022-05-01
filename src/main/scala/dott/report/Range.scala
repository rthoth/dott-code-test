package dott.report

import java.time.OffsetDateTime

object Range {

  def apply(dateTime: OffsetDateTime): Range = SpecificMoment(dateTime)
}

sealed trait Range {

  def toShow: String

}

case class BetweenRange(
  start: OffsetDateTime,
  end: OffsetDateTime,
  startOffset: Int,
  endOffset: Int
) extends Range {

  override def toShow: String = s"$endOffset-$startOffset"
}

case class BeforeThanRange(offset: OffsetDateTime, quantity: Int) extends Range {

  override def toShow: String = s">$quantity"
}

case class SpecificMoment(dateTime: OffsetDateTime) extends Range {

  override def toShow: String = s"[$dateTime]"
}

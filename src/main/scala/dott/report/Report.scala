package dott.report

import java.time.OffsetDateTime
import scala.collection.immutable.SortedMap

object Report {

  def apply(ranges: Iterable[Range]): Report = {
    new Report(SortedMap.from(ranges.map(range => (range, (range, 0L))))(RangeOrdering.reverse))
  }
}

class Report(val data: SortedMap[Range, (Range, Long)]) {

  def get(dateTime: OffsetDateTime): Option[(Range, Long)] = {
    data.get(Range(dateTime))
  }

  def withValue(range: Range, newCount: Long): Report = {
    new Report(data + (range -> (range, newCount)))
  }

}

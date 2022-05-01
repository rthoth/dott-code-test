package dott.report

import dott.database.Database
import dott.model.Order

import java.time.OffsetDateTime
import scala.util.Try


class Reporter(database: Database) {

  def report(start: OffsetDateTime, end: OffsetDateTime, groups: Seq[RangeDescriptor]): Try[Report] = {
    for {
      iterator <- database.iterate(start, end)
      report <- Try {
        val reference = OffsetDateTime.now()
        iterator.iterator.foldLeft(Report(groups.map(_.reference(reference))))(process)
      }
    } yield {
      report
    }
  }

  private def process(report: Report, order: Order): Report = {
    Console.err.println(s"${Console.RESET}${Console.GREEN}${order.date}${Console.RESET}")

    order.items.foldLeft(report) { (report, item) =>

      report.get(item.product.createdAt) match {
        case Some((range, count)) => report.withValue(range, count + item.quantity)
        case _ => report
      }
    }
  }
}

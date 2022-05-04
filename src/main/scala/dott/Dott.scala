package dott

import dott.database.Database
import dott.report.{BeforeThanRangeDescriptor, BetweenRangeDescriptor, RangeDescriptor, Reporter}

import java.io.File
import java.time.{LocalDateTime, OffsetDateTime}
import scala.Console._
import scala.util.{Failure, Success, Try}

object Dott extends App {

  val BetweenRangeRegex = """^(\d+)-(\d+)$""".r

  val BeforeThanRangeRegex = """^>(\d+)$""".r

  (for {
    (start, end, groups) <- parseArguments()
    database <- Database(new File("./"))
    _ <- Try(Console.out.println("Please wait, the report is being processed."))
    report <- new Reporter(database).report(start, end, groups)
  } yield {
    for ((range, (_, count)) <- report.data) {
      Console.out.println(s"${range.toShow} months: $count orders")
    }
  }) match {
    case Success(_) =>
      Console.out.println(s"$RESET${GREEN}Ok.$RESET")

    case Failure(cause) =>
      Console.err.print(s"$RESET$RED")
      cause.printStackTrace(Console.err)
      Console.err.println(RESET)
  }

  private def brd(start: Int, end: Int) = BetweenRangeDescriptor(start, end)

  //noinspection SameParameterValue
  private def btrd(quantity: Int) = BeforeThanRangeDescriptor(quantity)

  @inline
  private def invalidArgument[T](f: => T, message: String): T = {
    try {
      f
    } catch {
      case cause: Throwable => throw new IllegalArgumentException(message, cause)
    }
  }

  private def parseArguments(): Try[(OffsetDateTime, OffsetDateTime, Seq[RangeDescriptor])] = Try {
    if (args.length >= 4) {
      val start = invalidArgument(parseDateTime(s"${args(0)}T${args(1)}"), "Invalid starting point!")
      val end = invalidArgument(parseDateTime(s"${args(2)}T${args(3)}"), "Invalid ending point!")
      require(start.compareTo(end) < 0, s"Invalid date range [$start-$end]!")
      val groupValues = args.drop(4)
      if (groupValues.isEmpty) {
        (start, end, Seq(brd(1, 3), brd(4, 6), brd(7, 12), btrd(12)))
      } else {

        var groups = (for (group <- groupValues.view.dropRight(1)) yield group match {
          case BetweenRangeRegex(s, e) => BetweenRangeDescriptor(s.toInt, e.toInt): RangeDescriptor
          case _ => throw new IllegalArgumentException(s"Invalid group $group!")
        }).toVector

        groups :+= (groupValues.last match {
          case BetweenRangeRegex(s, e) => BetweenRangeDescriptor(s.toInt, e.toInt)
          case BeforeThanRangeRegex(q) => BeforeThanRangeDescriptor(q.toInt)
          case value => throw new IllegalArgumentException(s"Invalid group $value!")
        })

        (start, end, groups)
      }
    } else {
      throw new IllegalArgumentException("You have to inform at least two dates!")
    }
  }

  private def parseDateTime(string: String): OffsetDateTime = {
    //    LocalDateTime.parse(string).toInstant(ZonedDateTime.now().getOffset)

    OffsetDateTime
      .now()
      .`with`(LocalDateTime.parse(string))
  }
}

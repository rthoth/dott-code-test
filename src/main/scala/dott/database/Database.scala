package dott.database

import dott.model.Order

import java.io.File
import java.time.OffsetDateTime
import scala.util.Try

object Database {

  def apply(directory: File): Try[Database] = {
    Try(new DatabaseImpl(directory))
      .recoverWith { cause =>
        for (database <- DatabaseImpl.createRandomDatabase(directory, cause)) yield {
          Console.err.println(s"${Console.RESET}${Console.GREEN}${Console.BOLD}A fake database has just created.${Console.RESET}")
          database
        }
      }
  }
}

trait Database {

  def iterate(start: OffsetDateTime, end: OffsetDateTime): Try[IterableOnce[Order]]

}

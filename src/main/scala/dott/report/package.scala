package dott

package object report {

  implicit object RangeOrdering extends Ordering[Range] {

    override def compare(x: Range, y: Range): Int = {
      (x, y) match {
        case (BetweenRange(_, _, s1, e1), BetweenRange(_, _, s2, e2)) =>
          if (e1 < s2) -1
          else if (s1 > e2) 1
          else if (s1 < s2) -1
          else if (s1 > s2) 1
          else if (e1 < e2) -1
          else if (e1 > e2) 1
          else 0

        case (before: BeforeThanRange, after: BetweenRange) =>
           -compareWith(after, before)

        case (before: BetweenRange, after: BeforeThanRange) =>
          compareWith(before, after)

        case (BeforeThanRange(s1, _), BeforeThanRange(s2, _)) =>
          s1.compareTo(s2)

        case (before: SpecificMoment, after: BetweenRange) =>
          compareWith(before, after)

        case (before: SpecificMoment, after: BeforeThanRange) =>
          compareWith(before, after)

        case (before: BetweenRange, after: SpecificMoment) =>
          -compareWith(after, before)

        case (before: BeforeThanRange, after: SpecificMoment) =>
          -compareWith(after, before)

        case (before: SpecificMoment, after: SpecificMoment) =>
          before.dateTime.compareTo(after.dateTime)
      }
    }

    private def compareWith(specific: SpecificMoment, btre: BeforeThanRange): Int = {
      specific.dateTime.compareTo(btre.offset)
    }

    private def compareWith(specific: SpecificMoment, bre: BetweenRange): Int = {
      val SpecificMoment(offset) = specific
      val BetweenRange(s, e, _, _) = bre

      if (offset.compareTo(s) < 0) -1
      else if (offset.compareTo(e) > 0) 1
      else 0
    }

    private def compareWith(bre: BetweenRange, btre: BeforeThanRange): Int = {
      val BetweenRange(_, _, s1, e1) = bre
      val BeforeThanRange(_, e2) = btre

      if (e1 < e2) -1
      else if (s1 > e2) 1
      else 0 // it's unexpected.
    }
  }
}

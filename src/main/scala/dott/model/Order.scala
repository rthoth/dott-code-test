package dott.model

import java.time.OffsetDateTime

case class Order(
  customer: Customer,
  shippingAddress: Address,
  total: Money,
  date: OffsetDateTime,
  items: Seq[Item]
)

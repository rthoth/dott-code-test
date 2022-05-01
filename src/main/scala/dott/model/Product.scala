package dott.model

import java.time.OffsetDateTime

case class Product(
  id: String,
  name: String,
  category: String,
  weight: Gram,
  price: Money,
  createdAt: OffsetDateTime
)

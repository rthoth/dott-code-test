package dott.model

case class Address(
  country: String,
  state: String,
  city: String,
  address: String,
  complement: Option[String],
  zipCode: String
)

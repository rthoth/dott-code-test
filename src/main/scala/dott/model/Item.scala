package dott.model

case class Item(
  product: Product,
  price: Money,
  quantity: Int,
  cost: Money,
  fee: Money,
  tax: Money
)

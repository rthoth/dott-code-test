package dott

import spray.json.DefaultJsonProtocol._
import spray.json.{DeserializationException, JsString, JsValue, JsonFormat, RootJsonFormat}

import java.time.OffsetDateTime

package object model {

  type Money = BigDecimal

  type Gram = Int

  implicit val InstantFormat: JsonFormat[OffsetDateTime] = new JsonFormat[OffsetDateTime] {

    override def read(json: JsValue): OffsetDateTime = json match {
      case JsString(value) => OffsetDateTime.parse(value)
      case _ => throw DeserializationException("Invalid Offset!")
    }

    override def write(obj: OffsetDateTime): JsValue = JsString(obj.toString)
  }

  implicit val ProductFormat: RootJsonFormat[Product] = jsonFormat6(Product)

  implicit val AddressFormat: RootJsonFormat[Address] = jsonFormat6(Address)

  implicit val ItemFormat: RootJsonFormat[Item] = jsonFormat6(Item)

  implicit val CustomerFormat: RootJsonFormat[Customer] = jsonFormat3(Customer)

  implicit val OrderFormat: RootJsonFormat[Order] = jsonFormat5(Order)
}

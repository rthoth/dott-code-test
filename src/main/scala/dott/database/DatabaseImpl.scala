package dott.database

import dott.database.DatabaseImpl.PMap
import dott.model._
import org.mapdb.{DBMaker, Serializer}
import spray.json._

import java.io.File
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import scala.Console._
import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._
import scala.util.{Random, Try}

object DatabaseImpl {

  private class PMap(directory: File, name: String, shouldCreate: Boolean = false) extends AutoCloseable {

    private val db = DBMaker
      .fileDB(new File(directory, s"dott-$name.db"))
      .fileMmapEnable()
      .allocateStartSize(8 * 1024 * 1024)
      .allocateIncrement(8 * 1024 * 1024)
      .make()

    private val underlying = {
      val maker = db.hashMap("map", Serializer.STRING, Serializer.STRING)
      try {
        if (shouldCreate) maker.createOrOpen() else maker.open()
      } catch {
        case cause: Throwable =>
          db.close()
          throw cause
      }
    }

    override def close(): Unit = {
      underlying.close()
      db.close()
    }

    def get(key: String): String = underlying.get(key)

    def iterable: Iterator[(String, String)] = {
      for (entry <- underlying.entrySet().iterator().asScala) yield {
        (entry.getKey, entry.getValue)
      }
    }

    def put(key: String, value: String): PMap = {
      underlying.put(key, value)
      this
    }

    def size: Long = underlying.sizeLong()
  }

  @tailrec
  def createParallelOrders(start: OffsetDateTime, stop: OffsetDateTime, products: PMap, orders: PMap,
    processors: Int, count: Long): Unit = {

    if (start.compareTo(stop) < 0) {
      val steps = createSteps(start, stop, 600, processors)
      val futures = Future.traverse(steps) { current =>
        for {
          _ <- Future(createOrder(current, products, orders))
        } yield {
          current
        }
      }

      Await.result(futures, Duration.Inf)

      val nextStart = steps.last
      val newCount = count + steps.size
      //      if (newCount % (1000 * processors) == 0)
      //        Console.err.println(s"$RESET${CYAN}It has created $newCount orders until $nextStart.$RESET")
      createParallelOrders(nextStart, stop, products, orders, processors, newCount)
    }
  }

  def createRandomDatabase(directory: File, cause: Throwable): Try[Database] = Try {
    Console.err.println(s"$RESET${RED}Creating a new fake random database, please wait.$RESET")

    val now = OffsetDateTime.now()
    val begin = now.minus(5 * 365, ChronoUnit.DAYS)

    val products = new PMap(directory, "products", shouldCreate = true)
    val orders = new PMap(directory, "orders", shouldCreate = true)

    try {
      createOrders(begin, now, createProducts(begin, now, products), orders)
      Console.err.println(s"${RESET}${CYAN}It's been created ${products.size} products and ${orders.size} orders, since $begin.$RESET")
    } catch {
      case internal: Throwable =>
      //        internal.printStackTrace(Console.err)
    } finally {
      products.close()
      orders.close()
    }


    new DatabaseImpl(directory)
  }

  private def createItems(items: Int)(implicit products: PMap): Seq[Item] = {
    for (_ <- 0 until items) yield {
      val id = 1 + (products.size * Random.nextDouble()).toInt
      val product = products.get(s"dott-$id").parseJson.convertTo[Product]
      //      val product = newProduct(id, Instant.now())
      val price = product.price * (1 + 0.3 * (Random.nextDouble() * 2 - 1))
      val quantity = 1 + Random.nextInt(10)
      val cost = quantity * price
      val fee = cost * 0.2 * Random.nextDouble()
      val tax = cost * 0.3 * Random.nextDouble()
      Item(
        product = product,
        quantity = quantity,
        price = price,
        cost = cost,
        fee = fee,
        tax = tax
      )
    }
  }

  private def createOrder(current: OffsetDateTime, products: PMap, orders: PMap): Unit = {
    val customer = BigInt(Random.nextBytes(8)).abs.toString(16)
    val address = Address(
      country = "BRA",
      state = "SP",
      city = "São Paulo",
      address = "Av. Paulista, 13",
      complement = None,
      zipCode = "11.111-111"
    )

    val items = createItems(1 + Random.nextInt(5))(products)

    val order = Order(
      customer = Customer(
        name = customer,
        email = s"$customer@gmail.com",
        address = address
      ),
      shippingAddress = address,
      total = items.map(item => item.cost + item.fee + item.tax).sum,
      date = current,
      items
    )

    orders.put(s"dott-${BigInt(Random.nextBytes(16)).abs.toString(16)}", order.toJson.compactPrint)
  }

  private def createOrders(begin: OffsetDateTime, end: OffsetDateTime, products: PMap, orders: PMap): PMap = {
    createParallelOrders(begin, end, products, orders, Runtime.getRuntime.availableProcessors(), 0)
    orders
  }

  @tailrec
  private def createProduct(id: Long, current: OffsetDateTime, stop: OffsetDateTime)(implicit products: PMap): Long = {
    if (current.compareTo(stop) <= 0) {
      val product = newProduct(id, current)

      //      Console.err.println(s"$RESET${GREEN}It has created product ${product.id}.$RESET")

      products.put(product.id, product.toJson.compactPrint)
      val nextCurrent = current
        .plus(1 + Random.nextInt(6), ChronoUnit.DAYS)
        .plus(Random.nextInt(6), ChronoUnit.HOURS)

      createProduct(id + 1, nextCurrent, stop)
    } else {
      id - 1
    }
  }

  private def createProducts(begin: OffsetDateTime, end: OffsetDateTime, products: PMap): PMap = {
    createProduct(1, begin, end)(products)
    products
  }

  @tailrec
  private def createSteps(start: OffsetDateTime, stop: OffsetDateTime, offset: Int, limit: Int,
    result: Seq[OffsetDateTime] = Vector.empty): Seq[OffsetDateTime] = {

    if (result.size < limit) {
      if (start.compareTo(stop) < 0) {
        createSteps(start.plus(1 + Random.nextInt(offset), ChronoUnit.SECONDS), stop, offset, limit, result :+ start)
      } else {
        result :+ start
      }
    } else {
      result
    }
  }

  private def newProduct(id: Long, current: OffsetDateTime): Product = {
    Product(
      s"dott-$id",
      s"product-$id",
      s"category-${1 + Random.nextInt(10)}",
      weight = nextWeight(),
      price = nextPrice(),
      createdAt = current
    )
  }

  private def nextPrice(): Money = {
    5 + Random.nextInt(1500)
  }

  private def nextWeight(): Gram = {
    100 + Random.nextInt(5000)
  }
}

class DatabaseImpl(directory: File) extends Database {

  private val orders = new PMap(directory, "orders", shouldCreate = false)

  override def iterate(start: OffsetDateTime, end: OffsetDateTime): Try[IterableOnce[Order]] = Try {
    for {
      (_, value) <- orders.iterable
      order = value.parseJson.convertTo[Order]
      if order.date.compareTo(start) >= 0 && order.date.compareTo(end) <= 0
    } yield {
      order
    }
  }
}

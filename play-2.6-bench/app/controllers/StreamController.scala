package controllers

import javax.inject._

import akka.actor.ActorSystem
import akka.parboiled2.util.Base64
import akka.stream.scaladsl.Source
import play.api.mvc._
import net.jpountz.xxhash._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

/**
  * Proj: play-2.5-bench
  * User: gilad
  * Date: 9/26/17
  * Time: 9:46 AM
  */
@Singleton
class StreamController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext, sys: ActorSystem) extends AbstractController(cc) {

  val rfc4648 = new Base64("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_=")
  val SeedDelZero = "(\\d+)~([A-Za-z0-9\\-_=]+)".r
  val fiveMB: Long = 5 * 1 << 20
  val xxhashFactory = XXHashFactory.fastestJavaInstance()

  def xxhash64(arr: Array[Byte], seed: Long): Long = xxhashFactory.hash64().hash(arr, 0, arr.length, seed)

  def toLongBytes(long: Long): Array[Byte] = {
    var l = long
    val b7 = l.toByte
    l >>= 8
    val b6 = l.toByte
    l >>= 8
    val b5 = l.toByte
    l >>= 8
    val b4 = l.toByte
    l >>= 8
    val b3 = l.toByte
    l >>= 8
    val b2 = l.toByte
    l >>= 8
    val b1 = l.toByte
    l >>= 8
    val b0 = l.toByte
    Array(b0, b1, b2, b3, b4, b5, b6, b7)
  }

  def stream() = Action { implicit request: Request[AnyContent] =>
    request.getQueryString("size").fold[Try[Long]](Success(fiveMB))(s => Try(java.lang.Long.parseLong(s))).flatMap { size =>
      request.getQueryString("seed").fold[Try[(Long, Long, Array[Byte])]](Success((size, 786L, Array(1, 7, 2, 9)))) { s: String =>
        Try {
          val SeedDelZero(long, zero) = s
          val seed = java.lang.Long.parseLong(long)
          val init = rfc4648.decode(zero)
          (size, seed, init)
        }
      }
    } match {
      case Failure(err) => BadRequest(err.getMessage)
      case Success((size, seed, init)) => {
        val source = Source.unfold[(Long, Array[Byte]), Array[Byte]](size -> init) {
          case (0L, _) => None
          case (sizeLeft, bytes) =>
            if (bytes.length > sizeLeft) Some((0L -> Array.empty[Byte]) -> bytes.take(sizeLeft.toInt))
            else Some(((sizeLeft - bytes.length) -> toLongBytes(xxhash64(bytes, seed))) -> bytes)
        }
        Ok.chunked(source)
      }
    }
  }

  def astream() = Action.async { implicit request: Request[AnyContent] =>
    Future {
      request.getQueryString("size").fold[Try[Long]](Success(fiveMB))(s => Try(java.lang.Long.parseLong(s))).flatMap { size =>
        request.getQueryString("seed").fold[Try[(Long, Long, Array[Byte])]](Success((size, 786L, Array(1, 7, 2, 9)))) { s: String =>
          Try {
            val SeedDelZero(long, zero) = s
            val seed = java.lang.Long.parseLong(long)
            val init = rfc4648.decode(zero)
            (size, seed, init)
          }
        }
      } match {
        case Failure(err) => BadRequest(err.getMessage)
        case Success((size, seed, init)) => {
          val source = Source.unfoldAsync[(Long, Array[Byte]), Array[Byte]](size -> init) {
            case (0L, _) => Future.successful(None)
            case (sizeLeft, bytes) => Future {
              if (bytes.length > sizeLeft) Some((0L -> Array.empty[Byte]) -> bytes.take(sizeLeft.toInt))
              else Some(((sizeLeft - bytes.length) -> toLongBytes(xxhash64(bytes, seed))) -> bytes)
            }
          }
          Ok.chunked(source)
        }
      }
    }
  }

  def schedule(ignoredSegment: String) = Action.async { implicit request: Request[AnyContent] =>
    val delay = request.getQueryString("delay").fold(1.second){ s =>
      Try(Integer.parseUnsignedInt(s)) match {
        case Success(0) => Duration.Zero
        case Success(i) if i > 0 => i.seconds
        case _ => 1.second
      }
    }
    val echo = request.getQueryString("echo")
    val p = Promise[Result]()
    sys.scheduler.scheduleOnce(delay){
      p.success(echo.fold(NoContent)(Ok.apply))
    }
    p.future
  }

  def async(ignoredSegment: String) = Action.async { implicit request: Request[AnyContent] =>
    val real = request.getQueryString("real").fold(false)(_ => true)
    if(real) Future(NoContent)
    else Future.successful(NoContent)
  }

  def instant(ignoredSegment: String) = Action { implicit request: Request[AnyContent] =>
    val echo = request.getQueryString("echo")
    echo.fold(NoContent)(Ok.apply)
  }
}

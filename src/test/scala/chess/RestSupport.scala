package chess

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.testkit.TestKitBase
import akka.util.Timeout
import chess.common._
import chess.domain.RegisterData
import chess.rest.Errors.{RestException, ErrorResult}
import chess.rest.JsonProtocol
import org.scalatest.Matchers
import org.scalatest.exceptions.TestFailedException
import spray.json._

import scala.concurrent.Await
import scala.concurrent.duration._

trait RestSupport extends TestKitBase with Matchers with JsonProtocol {

  implicit def materializer: Materializer
  implicit def timeout: Timeout

  val baseUrl = "http://localhost:10080/api"

  object Rest {
    def register(data: RegisterData) = HttpRequest()
      .withMethod(HttpMethods.POST)
      .withUri(s"$baseUrl/register")
      .withEntity(ContentTypes.`application/json`, data.toJson.prettyPrint)
      .execute
  }

  implicit class RichHttpRequest(request: HttpRequest) {
    def execute: HttpResponse = Await.result(Http().singleRequest(request), 5.seconds)
  }

  implicit class RichHttpResponse(response: HttpResponse) {
    def body = response.entity.toStrict(5.seconds).await.data.utf8String
  }

  implicit class ResponseJsonParsers(response: HttpResponse) {
    def to[T: JsonReader]: T = {
      try {
        response.status shouldBe StatusCodes.OK
      } catch {
        case e: TestFailedException =>
          throw new RestException(response.toErrorResult)
      }
      response.body.parseJson.convertTo[T]
    }

    def toErrorResult = {
      val result = response.body.parseJson.convertTo[ErrorResult]
      response.status shouldBe result.status
      result
    }
  }
}

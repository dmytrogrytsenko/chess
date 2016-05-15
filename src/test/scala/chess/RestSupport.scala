package chess

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{GenericHttpCredentials, Authorization}
import akka.stream.Materializer
import akka.testkit.TestKitBase
import akka.util.Timeout
import chess.common._
import chess.domain.Identifiers.{UserId, Version, Token}
import chess.domain.{LoginResult, User, LoginData, RegisterData}
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

    def login(data: LoginData) = HttpRequest()
      .withMethod(HttpMethods.POST)
      .withUri(s"$baseUrl/login")
      .withEntity(ContentTypes.`application/json`, data.toJson.prettyPrint)
      .execute

    def logout(token: Token) = HttpRequest()
      .withMethod(HttpMethods.POST)
      .withUri(s"$baseUrl/logout")
      .withHeaders(Authorization(GenericHttpCredentials(token, "")))
      .execute

    def getProfile(token: Token) = HttpRequest()
      .withMethod(HttpMethods.GET)
      .withUri(s"$baseUrl/profile")
      .withHeaders(Authorization(GenericHttpCredentials(token, "")))
      .execute

    def getPlayers(token: Token, version: Option[Version] = None) = HttpRequest()
      .withMethod(HttpMethods.GET)
      .withUri(s"$baseUrl/players${version.map(v => s"?version=$v").getOrElse("")}")
      .withHeaders(Authorization(GenericHttpCredentials(token, "")))
      .execute

    def invite(token: Token, player: UserId) = HttpRequest()
      .withMethod(HttpMethods.POST)
      .withUri(s"$baseUrl/invite?player=$player")
      .withHeaders(Authorization(GenericHttpCredentials(token, "")))
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

    def shouldBeOK() = response.status shouldBe StatusCodes.OK

    def toErrorResult = {
      val result = response.body.parseJson.convertTo[ErrorResult]
      response.status shouldBe result.status
      result
    }
  }
}

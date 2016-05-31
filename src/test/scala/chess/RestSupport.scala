package chess

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, GenericHttpCredentials}
import akka.stream.Materializer
import akka.testkit.TestKitBase
import akka.util.Timeout
import chess.common._
import chess.domain.Identifiers.{GameId, InvitationId, Token, UserId, Version}
import chess.domain.{LoginData, LoginResult, RegisterData, User}
import chess.game.{PieceKind, Square}
import chess.rest.Errors.{ErrorResult, RestException}
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

    def cancelInvitation(token: Token, invitationId: InvitationId) = HttpRequest()
      .withMethod(HttpMethods.PUT)
      .withUri(s"$baseUrl/invitations/cancel?id=$invitationId")
      .withHeaders(Authorization(GenericHttpCredentials(token, "")))
      .execute

    def rejectInvitation(token: Token, invitationId: InvitationId) = HttpRequest()
      .withMethod(HttpMethods.PUT)
      .withUri(s"$baseUrl/invitations/reject?id=$invitationId")
      .withHeaders(Authorization(GenericHttpCredentials(token, "")))
      .execute

    def acceptInvitation(token: Token, invitationId: InvitationId) = HttpRequest()
      .withMethod(HttpMethods.PUT)
      .withUri(s"$baseUrl/invitations/accept?id=$invitationId")
      .withHeaders(Authorization(GenericHttpCredentials(token, "")))
      .execute

    def getGames(token: Token) = HttpRequest()
      .withMethod(HttpMethods.GET)
      .withUri(s"$baseUrl/games")
      .withHeaders(Authorization(GenericHttpCredentials(token, "")))
      .execute

    def getGame(token: Token, gameId: GameId, version: Option[Version] = None) = HttpRequest()
      .withMethod(HttpMethods.GET)
      .withUri(s"$baseUrl/games/$gameId${version.map(v => s"?version=$v").getOrElse("")}")
      .withHeaders(Authorization(GenericHttpCredentials(token, "")))
      .execute

    def move(token: Token, gameId: GameId, src: Square, dst: Square, promoted: Option[PieceKind] = None) = HttpRequest()
      .withMethod(HttpMethods.POST)
      .withUri(s"$baseUrl/games/$gameId/move?src=${src.name}&dst=${dst.name}${promoted.map(v => s"&promoted=${v.name}").getOrElse("")}")
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

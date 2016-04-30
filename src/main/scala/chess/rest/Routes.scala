package chess.rest

import akka.actor.{ActorContext, Props}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.headers.{HttpCredentials, HttpChallenge}
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.directives.AuthenticationResult
import akka.http.scaladsl.util.FastFuture
import akka.pattern.ask
import akka.util.Timeout
import chess.common.Messages.Start
import chess.common._
import chess.common.actors.BaseActor
import chess.domain.Identifiers._
import chess.repositories.SessionRepository
import chess.repositories.SessionRepository.{Activity, SessionNotFound, SessionFound, GetSession}

import scala.concurrent.{Future, ExecutionContext, ExecutionContextExecutor}
import scala.reflect.ClassTag

trait Routes extends BaseActor with Directives with JsonProtocol with SprayJsonSupport {

  implicit def executor: ExecutionContextExecutor
  implicit def timeout: Timeout

  val challenge = HttpChallenge(scheme = "", realm = "", params = Map.empty)

  implicit class RichControllerProps(props: Props) {
    def execute[T](implicit tag: ClassTag[T],
                   context: ActorContext,
                   executionContext: ExecutionContext,
                   timeout: Timeout): Future[T] =
      (context.actorOf(props) ? Start flatMap normalizeAskResult).mapTo[T]
  }

  def authenticate[T](authenticator: Option[HttpCredentials] => Future[AuthenticationResult[T]]) =
    authenticateOrRejectWithChallenge(authenticator)

  def getUserIdByToken(token: Token): Future[Option[UserId]] =
    SessionRepository.endpoint ? GetSession(token) flatMap normalizeAskResult flatMap {
      case SessionFound(session) =>
        SessionRepository.endpoint ? Activity(token) flatMap normalizeAskResult map { _ => Some(session.userId) }
      case SessionNotFound(`token`) => Future { None }
    }

  def tokenAuthenticator(credentials: Option[HttpCredentials]): Future[AuthenticationResult[Token]] = {
    credentials match {
      case Some(c) => FastFuture.successful(AuthenticationResult.success(c.token().toToken))
      case None => FastFuture.successful(AuthenticationResult.failWithChallenge(challenge))
    }
  }

  def userAuthenticator(credentials: Option[HttpCredentials]): Future[AuthenticationResult[UserId]] = {
    credentials match {
      case Some(c) => getUserIdByToken(c.value.toToken) map {
        case Some(userId) => AuthenticationResult.success(userId)
        case None => AuthenticationResult.failWithChallenge(challenge)
      }
      case None => FastFuture.successful(AuthenticationResult.failWithChallenge(challenge))
    }
  }
}
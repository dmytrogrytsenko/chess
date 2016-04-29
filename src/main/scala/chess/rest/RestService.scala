package chess.rest

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.pattern.AskTimeoutException
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import chess.common.Messages.Start
import chess.common.actors.BaseActor
import chess.rest.Errors.{InternalServerError, RestException}
import chess.settings.EndpointSettings

trait RestService extends Routes {
  def settings: EndpointSettings
  def routes: Route

  implicit val materializer = ActorMaterializer()

  def receive = {
    case Start =>
      Http(context.system).bindAndHandle(handler, settings.interface, settings.port)
  }

  def handler = handleExceptions(exceptionHandler)(routes)

  val exceptionHandler = ExceptionHandler {
    case e: RestException =>
      log.error(e, e.result.message)
      complete(e.result.status -> e.result)
    case e: AskTimeoutException =>
      log.error(e, InternalServerError.timeout.message)
      complete(InternalServerError.timeout.status -> InternalServerError.timeout)
  }
}

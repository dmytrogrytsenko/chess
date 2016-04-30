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

trait RestService extends Routes with Jasonify {
  def settings: EndpointSettings
  def routes: Route

  implicit val materializer = ActorMaterializer()

  def receive = {
    case Start => Http(context.system).bindAndHandle(routes, settings.interface, settings.port)
  }
}

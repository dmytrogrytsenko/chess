package chess.rest

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import chess.common.Messages.Start
import chess.common.actors.BaseActor
import chess.settings.EndpointSettings

trait RestService extends BaseActor {
  def settings: EndpointSettings
  def routes: Route

  implicit val materializer = ActorMaterializer()

  def receive = {
    case Start =>
      Http(context.system).bindAndHandle(routes, settings.interface, settings.port)
  }
}

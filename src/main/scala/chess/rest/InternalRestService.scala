package chess.rest

import akka.util.Timeout
import chess.common.actors.NodeSingleton1
import chess.routes.{SocketRoutes, WebRoutes}
import chess.settings.EndpointSettings

import scala.concurrent.ExecutionContextExecutor

object InternalRestService extends NodeSingleton1[InternalRestService, EndpointSettings]

class InternalRestService(val settings: EndpointSettings) extends RestService
  with WebRoutes
  with SocketRoutes {

  implicit def executor: ExecutionContextExecutor = context.dispatcher
  implicit def timeout: Timeout = settings.defaultTimeout

  val webPath = "internal"
  val routes = webRoutes ~ socketRoutes
}

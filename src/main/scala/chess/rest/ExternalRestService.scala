package chess.rest

import akka.util.Timeout
import chess.common.actors.NodeSingleton1
import chess.routes._
import chess.settings.EndpointSettings

import scala.concurrent.ExecutionContextExecutor

object ExternalRestService extends NodeSingleton1[ExternalRestService, EndpointSettings]

class ExternalRestService(val settings: EndpointSettings) extends RestService
  with UserRoutes
  with PlayerRoutes
  with GameRoutes
  with WebRoutes
  with SocketRoutes {

  implicit def executor: ExecutionContextExecutor = context.dispatcher
  implicit def timeout: Timeout = settings.defaultTimeout

  val webPath = "external"
  val routes = pathPrefix("api") { userRoutes ~ playerRoutes ~ gameRoutes } ~ webRoutes ~ socketRoutes
}

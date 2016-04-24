package chess.rest

import chess.common.actors.NodeSingleton1
import chess.routes.{SocketRoutes, WebRoutes}
import chess.settings.EndpointSettings

object ExternalRestService extends NodeSingleton1[ExternalRestService, EndpointSettings]

class ExternalRestService(val settings: EndpointSettings) extends RestService
  with WebRoutes
  with SocketRoutes {

  val webPath = "external"
  val routes = webRoutes ~ socketRoutes
}

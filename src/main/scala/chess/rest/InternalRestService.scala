package chess.rest

import chess.common.actors.NodeSingleton1
import chess.routes.{SocketRoutes, WebRoutes}
import chess.settings.EndpointSettings

object InternalRestService extends NodeSingleton1[InternalRestService, EndpointSettings]

class InternalRestService(val settings: EndpointSettings) extends RestService
  with WebRoutes
  with SocketRoutes {

  val webPath = "internal"
  val routes = webRoutes ~ socketRoutes
}

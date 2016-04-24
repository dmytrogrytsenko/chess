package chess.routes

import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl.Flow
import chess.rest.Routes

trait SocketRoutes extends Routes {
  def service: Flow[Message, Message, Any] = Flow[Message]

  val socketRoutes =
    path("ws") {
      get {
        handleWebSocketMessages(service)
      }
    }
}

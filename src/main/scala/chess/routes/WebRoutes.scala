package chess.routes

import chess.rest.Routes

trait WebRoutes extends Routes {
  def webPath: String

  val webRoutes =
    get {
      pathEndOrSingleSlash {
        getFromResource(s"$webPath/index.html")
      }
    } ~
      get {
        getFromResourceDirectory(webPath)
      }
}

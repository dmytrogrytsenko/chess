package chess.routes

import chess.rest.Routes

trait WebRoutes extends Routes {
  def webPath: String

  val webRoutes =
    get {
      pathEndOrSingleSlash {
        getFromFile(s"./src/main/resources/$webPath/index.html")
        //getFromResource(s"$webPath/index.html")
      }
    } ~
      get {
        getFromDirectory(webPath)
        //getFromResourceDirectory(webPath)
      }
}

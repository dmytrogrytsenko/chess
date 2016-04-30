package chess.routes

import akka.http.scaladsl.model.StatusCode
import chess.common._
import chess.controllers.RegisterUserController
import chess.domain.{UserRegistrationResult, UserRegistrationData}
import chess.rest.Routes

trait UserRoutes extends Routes {
  val userRoutes =
    path("register") {
      post {
        entity(as[UserRegistrationData]) { data =>
          complete {
            RegisterUserController.props(data).execute[(StatusCode, UserRegistrationResult)]
          }
        }
      }
    }
}

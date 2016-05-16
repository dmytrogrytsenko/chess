package chess.routes

import chess.common.Messages.Done
import chess.controllers.{LogoutController, LoginController, RegisterController, GetProfileController}
import chess.domain._
import chess.rest.Errors.BadRequest
import chess.rest.Routes

trait UserRoutes extends Routes {
  val userRoutes = registerRoute ~ loginRoute ~ logoutRoute ~ profileRoute

  def registerRoute =
    path("register") {
      post {
        entity(as[RegisterData]) { data =>
          validate(data.name.nonEmpty, BadRequest.Validation.requiredMemberEmpty("name").message) {
            validate(data.password.nonEmpty, BadRequest.Validation.requiredMemberEmpty("password").message) {
              complete {
                RegisterController.props(data).execute[RegisterResult]
              }
            }
          }
        }
      }
    }

  def loginRoute =
    path("login") {
      post {
        entity(as[LoginData]) { data =>
          validate(data.name.nonEmpty, BadRequest.Validation.requiredMemberEmpty("name").message) {
            validate(data.password.nonEmpty, BadRequest.Validation.requiredMemberEmpty("password").message) {
              complete {
                LoginController.props(data).execute[LoginResult]
              }
            }
          }
        }
      }
    }

  def logoutRoute =
    path("logout") {
      post {
        authenticate(tokenAuthenticator) { token =>
          complete {
            LogoutController.props(token).execute[Done]
          }
        }
      }
    }

  def profileRoute =
    path("profile") {
      get {
        authenticate(userAuthenticator) { userId =>
          complete {
            GetProfileController.props(userId).execute[ProfileResult]
          }
        }
      }
    }
}

package chess.rest

import akka.http.scaladsl.model.{StatusCodes, StatusCode}

object Errors {
  type ErrorCode = String

  case class ErrorResult(status: StatusCode, code: ErrorCode, message: String)

  class RestException(val result: ErrorResult)
    extends RuntimeException(s"${result.status.intValue()} (${result.status.reason()}) ${result.code} ${result.message}")

  //400
  object BadRequest {
    def validation(message: String) = ErrorResult(StatusCodes.BadRequest, "VALIDATION", message)
    object Validation {
      def requiredMemberEmpty(name: String) = validation(s"The request content validation is failed: Required member '$name' is empty")
    }
  }

  //401
  object Unauthorized {
    def credentialsRejected = ErrorResult(StatusCodes.Unauthorized, "CREDENTIALS_REJECTED", "The supplied authentication is invalid")
  }

  //403
  object Forbidden {
    def accessDenied = ErrorResult(StatusCodes.Forbidden, "ACCESS_DENIED","Access denied.")
  }

  //404
  object NotFound {
    def resourceNotFound = ErrorResult(StatusCodes.NotFound, "RESOURCE_NOT_FOUND", "The requested resource could not be found")
    def userNotFound = ErrorResult(StatusCodes.NotFound, "USER_NOT_FOUND", "User not found")
    def invitationNotFound = ErrorResult(StatusCodes.NotFound, "INVITATION_NOT_FOUND", "Invitation not found")
  }

  //409
  object Conflict {
    def userAlreadyExists(name: String) = ErrorResult(StatusCodes.Conflict, "USER_ALREADY_EXISTS", s"User $name already exists")
    def cantInviteSelf = ErrorResult(StatusCodes.Conflict, "CANT_INVITE_SELF", s"User can't invite self")
    def incorrectInvitationStatus = ErrorResult(StatusCodes.Conflict, "INCORRECT_INVITATION_STATUS", s"Incorrect invitation status")
  }

  //500
  object InternalServerError {
    def default = ErrorResult(StatusCodes.InternalServerError, "INTERNAL_SERVER_ERROR", "There was an internal server error")
    def timeout = ErrorResult(StatusCodes.InternalServerError, "TIMEOUT", "The request processing is timed out")
  }
}

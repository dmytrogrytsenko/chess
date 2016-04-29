package chess.rest

import akka.http.scaladsl.model.{StatusCodes, StatusCode}

object Errors {
  type ErrorCode = String

  case class ErrorResult(status: StatusCode, code: ErrorCode, message: String)

  class RestException(val result: ErrorResult)
    extends RuntimeException(s"${result.status.intValue()} (${result.status.reason()}) ${result.code} ${result.message}")

  //409
  object Conflict {
    def userAlreadyExists(name: String) =
      ErrorResult(StatusCodes.Conflict, "USER_ALREADY_EXISTS", s"User $name already exists.")
  }

  //500
  object InternalServerError {
    def default = ErrorResult(StatusCodes.InternalServerError, "INTERNAL_SERVER_ERROR", "There was an internal server error.")
    def timeout = ErrorResult(StatusCodes.InternalServerError, "TIMEOUT", "The request processing is timed out.")
  }
}

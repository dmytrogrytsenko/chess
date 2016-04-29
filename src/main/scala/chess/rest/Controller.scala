package chess.rest

import akka.http.scaladsl.model.StatusCode
import chess.common.actors.SingleUseActor
import chess.rest.Errors.{ErrorResult, RestException}

trait Controller extends SingleUseActor {
  def failure(result: ErrorResult): Unit = failure(new RestException(result))
}

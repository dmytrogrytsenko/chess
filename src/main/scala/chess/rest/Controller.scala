package chess.rest

import chess.common.actors.SingleUseActor
import chess.rest.Errors.{ErrorResult, RestException}

trait Controller extends SingleUseActor {
  def failure(result: ErrorResult): Unit = failure(new RestException(result))
}

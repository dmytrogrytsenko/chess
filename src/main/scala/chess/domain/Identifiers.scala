package chess.domain

import chess.common._

object Identifiers {

  sealed trait USER_ID

  type UserId = String @@ USER_ID

  implicit class TaggedStringOpts(value: String) {
    def toUserId = value.asInstanceOf[UserId]
  }

  object UserId {
    def generate() = newUUID.toUserId
  }
}

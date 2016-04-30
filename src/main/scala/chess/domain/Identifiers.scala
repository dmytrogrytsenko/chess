package chess.domain

import chess.common._

object Identifiers {

  sealed trait USER_ID
  sealed trait TOKEN

  type UserId = String @@ USER_ID
  type Token = String @@ TOKEN

  implicit class TaggedStringOpts(value: String) {
    def toUserId = value.asInstanceOf[UserId]
    def toToken = value.asInstanceOf[Token]
  }

  object UserId {
    def generate() = newUUID.toUserId
  }

  object Token {
    def generate() = newUUID.toToken
  }
}

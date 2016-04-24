package chess.domain

import chess.common._

case class UserId(underlying: String) extends AnyVal

object UserId {
  def generate() = UserId(newUUID)
}

case class User(id: UserId, name: String)

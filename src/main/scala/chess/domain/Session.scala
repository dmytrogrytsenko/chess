package chess.domain

import chess.domain.Identifiers.{Token, UserId}
import org.joda.time.DateTime

case class Session(token: Token, userId: UserId, createdAt: DateTime, lastActivityAt: DateTime)

object Session {
  def create(userId: UserId) = Session(
    token = Token.generate(),
    userId = userId,
    createdAt = DateTime.now,
    lastActivityAt = DateTime.now)
}
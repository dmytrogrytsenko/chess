package chess.domain

import chess.domain.Identifiers._
import org.joda.time.DateTime

case class User(id: UserId, name: String, password: String, createdAt: DateTime)

object User {
  def apply(data: UserRegistrationData): User =
    User(
      id = UserId.generate(),
      name = data.name,
      password = data.password,
      createdAt = DateTime.now)
}

case class UserRegistrationData(name: String, password: String)

case class UserRegistrationResult(id: UserId, name: String, createdAt: DateTime)

object UserRegistrationResult {
  def apply(user: User): UserRegistrationResult =
    UserRegistrationResult(
      id = user.id,
      name = user.name,
      createdAt = user.createdAt)
}

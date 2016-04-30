package chess.domain

import chess.domain.Identifiers._
import org.joda.time.DateTime

case class User(id: UserId, name: String, password: String, displayName: Option[String], createdAt: DateTime)

object User {
  def apply(data: UserRegistrationData): User =
    User(
      id = UserId.generate(),
      name = data.name,
      password = data.password,
      displayName = data.displayName,
      createdAt = DateTime.now.withMillisOfSecond(0))
}

case class UserRegistrationData(name: String, password: String, displayName: Option[String])

case class UserRegistrationResult(id: UserId, name: String, displayName: Option[String], createdAt: DateTime)

object UserRegistrationResult {
  def apply(user: User): UserRegistrationResult =
    UserRegistrationResult(
      id = user.id,
      name = user.name,
      displayName = user.displayName,
      createdAt = user.createdAt)
}

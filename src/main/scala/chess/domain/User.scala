package chess.domain

import chess.domain.Identifiers._
import org.joda.time.DateTime

case class User(id: UserId,
                name: String,
                password: String,
                displayName: Option[String],
                createdAt: DateTime)

object User {
  def apply(data: RegisterData): User =
    User(
      id = UserId.generate(),
      name = data.name,
      password = data.password,
      displayName = data.displayName,
      createdAt = DateTime.now)
}

case class UserData(id: UserId, name: String, displayName: Option[String], createdAt: DateTime)

object UserData {
  def apply(user: User): UserData =
    UserData(
      id = user.id,
      name = user.name,
      displayName = user.displayName,
      createdAt = user.createdAt)

  def unknown(userId: UserId) = UserData(userId, "unknown", Some("Unknown"), DateTime.now)
}

case class RegisterData(name: String, password: String, displayName: Option[String])

case class RegisterResult(user: UserData)

case class LoginData(name: String, password: String)

case class LoginResult(token: Token, user: UserData)

case class ProfileResult(user: UserData)

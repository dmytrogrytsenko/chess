package chess.repositories

import chess.common._
import chess.common.actors.{NodeSingleton1, BaseActor}
import chess.common.Messages.Start
import chess.common.mongo._
import chess.domain.Identifiers.UserId
import chess.domain.User
import chess.mongo.UserCollection
import reactivemongo.api.DB
import reactivemongo.core.errors.DatabaseException

object UserRepository extends NodeSingleton1[UserRepository, DB] {
  case class AddUser(user: User)
  case class UserAdded(userId: UserId)
  case class UserAlreadyExists(userId: UserId)

  case class FindUserById(userId: UserId)
  sealed trait FindUserByIdResult
  case class UserFoundById(user: User) extends FindUserByIdResult
  case class UserNotFoundById(userId: UserId) extends FindUserByIdResult

  case class FindUserByName(username: String)
  sealed trait FindUserByNameResult
  case class UserFoundByName(user: User) extends FindUserByNameResult
  case class UserNotFoundByName(username: String) extends FindUserByNameResult
}

class UserRepository(implicit val db: DB) extends BaseActor {

  import UserCollection._
  import UserRepository._
  import context.dispatcher

  def receive = {
    case Start => ensureIndexes

    case AddUser(user) =>
      add(user).map(_ => UserAdded(user.id)).recover {
        case e: DatabaseException if isDuplicateKeyError(e.code) => UserAlreadyExists(user.id)
      } pipeTo sender()

    case FindUserById(userId) =>
      get(userId) map {
        case Some(user) => UserFoundById(user)
        case None => UserNotFoundById(userId)
      } pipeTo sender()

    case FindUserByName(username) =>
      findUserByName(username) map {
        case Some(user) => UserFoundByName(user)
        case None => UserNotFoundByName(username)
      } pipeTo sender()
  }
}

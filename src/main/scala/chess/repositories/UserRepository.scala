package chess.repositories

import chess.common._
import chess.common.Messages.Start
import chess.common.actors.{NodeSingleton1, BaseActor}
import chess.domain.{UserId, User}
import chess.mongo.UserCollection
import reactivemongo.api.DB

object UserRepository extends NodeSingleton1[UserRepository, DB] {
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
